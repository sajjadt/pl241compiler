package org.pl241.ra;

import java.util.HashMap;
import java.util.List;

import java.util.ArrayList;
import java.util.Map;

import org.pl241.ir.AbstractNode;
import org.pl241.ir.BasicBlock;
import org.pl241.ir.Function;

public class RegisterAllocator { // Allocate Registers for one function

    public RegisterAllocator(int _numRegs) { // SSA Form
		numPhysicalRegisters = _numRegs ;
	}

	public HashMap<String, ArrayList<LiveInterval>> allocate(Function function) {

        // Create Live Intervals
        IntervalBuilder intervalBuilder = new IntervalBuilder();
        intervalBuilder.build(function.getBlocksInLayoutOrder());
        intervalBuilder.printIntervals();

        // Linear Scanner works with non-splitted intervals
        scanner = new LinearScanner(intervalBuilder.getLiveIntervals(), numPhysicalRegisters);
        scanner.scan();

        List<LiveInterval> alloc = scanner.handled;

        System.out.println(alloc);

        // At this point we need a list of allocations with these properties for resolve function:
        // access to all live intervals at given time
        // access to certain var allocation at give time
        HashMap<String, ArrayList<LiveInterval>> intervalMap = new HashMap<>();
        for (LiveInterval interval: alloc) {
            if (!intervalMap.containsKey(interval.varName))
                intervalMap.put(interval.varName, new ArrayList<>());
            intervalMap.get(interval.varName).add(interval);
        }

        //assert intervalMap.keySet().equals(alloc)

        return intervalMap;
    }


    public void resolve(Function function, HashMap<String, ArrayList<LiveInterval>> intervals) {

        Allocation moveFrom = null;
        Allocation moveTo= null;

        Map<Allocation, Allocation> mapping = new HashMap<>();

        for (BasicBlock pred: function.basicBlocks) {
            for (BasicBlock succ: pred.getSuccessors()) {

                // Intervals live at the beginning of the succ
                ArrayList<LiveInterval> liveIntervals = liveIntervalsAt(intervals, succ.bFrom);

                for (LiveInterval interval: liveIntervals) {
                    if (interval.start() == succ.bFrom) {
                        AbstractNode node = succ.getPhiOperand(interval.varName, pred.getIndex());
                        if (node != null)
                            moveFrom = liveIntervalAllocationAt(intervals, node.getOutputOperand(), pred.bTo);
                    } else {
                        moveFrom = liveIntervalAllocationAt(intervals, interval.varName, pred.bTo);
                    }

                    moveTo = liveIntervalAllocationAt(intervals, interval.varName, succ.bFrom);

                    if ( moveFrom!= null && !moveFrom.equals(moveTo)) {
                        mapping.put(moveFrom, moveTo);
                    }
                }
            }
        }

        // TODO: insert moves from mapping into basic blocks
        System.out.println(mapping);
    }

    // Utility functions
    private ArrayList<LiveInterval> liveIntervalsAt(HashMap<String, ArrayList<LiveInterval>> intervalsSet, int time) {
        ArrayList<LiveInterval> intervals = new ArrayList<>();

        for (ArrayList<LiveInterval> intervalsArray: intervalsSet.values()) {
            intervals.addAll(intervalsArray);
        }

        ArrayList<LiveInterval> liveIntervals = new ArrayList<>();
        for (LiveInterval interval: intervals) {
            if (interval.isAlive(time)) {
                liveIntervals.add(interval);
            }
        }
        return liveIntervals;
    }

    private Allocation liveIntervalAllocationAt(HashMap<String, ArrayList<LiveInterval>> intervalsSet, String varName, int time) {
        ArrayList<LiveInterval> intervals = intervalsSet.get(varName);

        for (LiveInterval interval: intervals) {
            if (interval.isAlive(time)) {
                return interval.allocatedLocation;
            }
        }
        return null;
    }

    private int numPhysicalRegisters;
    private LinearScanner scanner ;

    public void toPhysical(Function f, HashMap<String, ArrayList<LiveInterval>> intervals) {
        for (BasicBlock block: f.basicBlocks) {
            for (AbstractNode node: block.getNodes()) {
                if (node.hasOutputRegister()) {
                    node.setAllocation(liveIntervalAllocationAt(intervals, node.getOutputOperand(), node.sourceIndex));
                }
            }
        }
     }
}