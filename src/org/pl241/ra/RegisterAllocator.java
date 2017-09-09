package org.pl241.ra;

import java.util.HashMap;
import java.util.List;

import java.util.ArrayList;
import java.util.Map;

import org.pl241.ir.*;

public class RegisterAllocator { // Allocate Registers for one function

    public RegisterAllocator(int _numRegs) { // SSA Form
		intervalMap = new HashMap<>();
        numPhysicalRegisters = _numRegs ;
	}

	public HashMap<String, ArrayList<LiveInterval>> allocate(Function function) {

        // Create Live Intervals
        IntervalBuilder intervalBuilder = new IntervalBuilder();
        System.out.println("For function " + function.name);
        intervalBuilder.build(function.getBlocksInLayoutOrder());
        intervalBuilder.printIntervals();

        // Linear Scanner works with non-splitted intervals
        LinearScanner scanner = new LinearScanner(intervalBuilder.getLiveIntervals(), numPhysicalRegisters);
        scanner.scan();

        List<LiveInterval> alloc = scanner.handled;

        //System.out.println(alloc);

        // At this point we need a list of allocations with these properties for resolve function:
        // access to all live intervals at given time
        // access to certain var allocation at give time
        intervalMap.clear();
        for (LiveInterval interval: alloc) {
            if (!intervalMap.containsKey(interval.varName))
                intervalMap.put(interval.varName, new ArrayList<>());
            intervalMap.get(interval.varName).add(interval);
        }

        //assert intervalMap.keySet().equals(alloc)
        System.out.println(function.name + "  allocations :");
        intervalBuilder.printIntervals();
        return intervalMap;
    }

    public void resolve(Function function) {

        Allocation moveFrom = null;
        Allocation moveTo= null;

        Map<Allocation, Allocation> mapping = new HashMap<>();

        for (BasicBlock current: function.basicBlocks) {
            for (BasicBlock successor: current.getSuccessors()) {

                // Intervals live at the beginning of the successor
                ArrayList<LiveInterval> liveIntervals = liveIntervalsAt(intervalMap, successor.bFrom);
                for (LiveInterval interval: liveIntervals) {

                    // It will be a Phi Node
                    if (interval.start() == successor.bFrom) {
                        AbstractNode node = successor.getPhiOperand(interval.varName, current.getIndex());
                        if (node != null)
                            moveFrom = liveIntervalAllocationAt(intervalMap, node.getOutputOperand(), current.bTo);
                    } else {
                        moveFrom = liveIntervalAllocationAt(intervalMap, interval.varName, current.bTo);
                    }

                    moveTo = liveIntervalAllocationAt(intervalMap, interval.varName, successor.bFrom);

                    if ( moveFrom!= null && !moveFrom.equals(moveTo)) {
                        mapping.put(moveFrom, moveTo);
                        AbstractNode lastNode = current.getNodes().get(current.getNodes().size()-1);

                        if (lastNode instanceof BranchNode || lastNode instanceof FunctionCallNode)
                            current.getNodes().add(current.getNodes().size()-1, new MoveNode(moveFrom, moveTo));
                        else
                            current.getNodes().add(current.getNodes().size(), new MoveNode(moveFrom, moveTo));
                    }

                }
            }
        }

        System.out.println("************ Moves ***********");
        System.out.println(mapping);
        System.out.println("********** End Moves **********");

        // Remove Phi nodes
        for (BasicBlock current: function.basicBlocks) {
            List<AbstractNode> found = new ArrayList<>();
            for (AbstractNode node: current.getNodes()) {
                if (node instanceof PhiNode)
                    found.add(node);
            }
            current.getNodes().removeAll(found);
        }
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

        if (intervals == null) {
            System.out.println("No interval found for " + varName + " at time " + time);
            return null;
        }
        for (LiveInterval interval: intervals) {
            if (interval.isAlive(time)) {
                return interval.allocatedLocation;
            }
        }
        return null;
    }

    public Allocation getAllocationAt(String variableName, int time) {
        return liveIntervalAllocationAt(intervalMap, variableName, time) ;
    }

    public void toPhysical(Function f) {
        for (BasicBlock block: f.basicBlocks) {
            for (AbstractNode node: block.getNodes()) {
                if (node.hasOutputRegister()) {
                    node.setAllocation(liveIntervalAllocationAt(intervalMap, node.getOutputOperand(), node.sourceIndex));
                }
            }
        }
     }



    private HashMap<String, ArrayList<LiveInterval>> intervalMap;
    private int numPhysicalRegisters;

}