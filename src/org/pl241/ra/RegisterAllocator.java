package org.pl241.ra;

import java.util.HashMap;
import java.util.List;

import java.util.ArrayList;
import java.util.Map;


import org.pl241.ir.*;

public class RegisterAllocator { // Allocate Registers for one function

    public RegisterAllocator(int _numRegs) { // SSA Form
		intervalMap = new HashMap<>();
        numPhysicalRegisters = _numRegs;
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

        class Move {
            public Move(Allocation from, Allocation to, boolean isExchange) {
                this.from = from;
                this.to = to;
                this.isExchange = isExchange;
            }
            Allocation from;
            Allocation to;
            Boolean isExchange;

            @Override
            public String toString() {
                return from + " to " + to;
            }
        }

        // Map of curblock.id:nextblock.id to moves
        Map<String, List<Move>> mapping = new HashMap<>();
        for (BasicBlock current: function.basicBlocks) {
            for (BasicBlock successor: current.getSuccessors()) {
                // Intervals live at the beginning of the successor
                ArrayList<LiveInterval> liveIntervals = liveIntervalsAt(intervalMap, successor.bFrom);

                for (LiveInterval interval: liveIntervals) {
                    // It will be a Phi Node
                    if (interval.start() == successor.bFrom) {
                        AbstractNode node = successor.getPhiOperand(interval.varName, current.getIndex());
                        AbstractNode destNode = successor.getPhiNode(interval.varName);
                        assert node != null;
                        assert destNode != null;
                        moveFrom = liveIntervalAllocationAt(intervalMap, node.getOutputVirtualReg(), current.bTo);
                        moveTo = liveIntervalAllocationAt(intervalMap, destNode.getOutputVirtualReg(), successor.bFrom);

                        System.out.println("at " + successor.bFrom + " From " + node + " to " + destNode+ " alloc" + moveFrom + " to " + moveTo);
                    } else {
                        moveFrom = liveIntervalAllocationAt(intervalMap, interval.varName, current.bTo);
                        moveTo = liveIntervalAllocationAt(intervalMap, interval.varName, successor.bFrom);
                        System.out.println("at " + successor.bFrom + "From " + interval.varName + " to " + interval.varName + " alloc" + moveFrom + " to " + moveTo);
                    }

                    if (moveFrom != null && !moveFrom.equals(moveTo)) {
                        if (!mapping.containsKey(current.getID()+":"+successor.getID()))
                            mapping.put(current.getID()+":"+successor.getID(), new ArrayList<>());

                        mapping.get(current.getID()+":"+successor.getID()).add(new Move(moveFrom, moveTo, false));
                    }

                }
            }
        }

        System.out.println("************ Moves ***********");
        // Reorder and insert moves
        for (String blockID: mapping.keySet()) {
            // Reorder and insert temp reg if they from a cycle
            List<Move> moves = mapping.get(blockID);
            System.out.println("block " + blockID + " " + moves);
            List<Move> newMoves = new ArrayList<>();

            while (!moves.isEmpty()){
                // Find a move which the destination is not a source for another move
                Move selected = null;

                for (Move move: moves) {
                    boolean usedForRead = false;
                    //boolean usedForWrite = true;

                    for (Move otherMove: moves) {
                        if (otherMove.equals(move))
                            continue;
                        if (otherMove.from.equals(move.to))
                            usedForRead = true;
                    }
                    if (!usedForRead) {
                        selected = move;
                        break;
                    }
                }

                if (selected != null) {
                    moves.remove(selected);
                    newMoves.add(selected);
                } else {
                    // There was a cycle(s)
                    List<Move> cycle = new ArrayList<>();

                    selected = moves.get(0);
                    cycle.add(selected);
                    moves.remove(selected);

                    // Find the others on this cycle
                    boolean foundOneOnCycle = true;
                    while (foundOneOnCycle) {
                        foundOneOnCycle = false;
                        Move move = cycle.get(cycle.size()-1);
                        for (Move otherMove: moves) {
                            if (otherMove.from.equals(move.to)) {
                                foundOneOnCycle = true;
                                selected = otherMove;
                                break;
                            }
                        }
                        if (foundOneOnCycle) {
                            moves.remove(selected);
                            cycle.add(selected);
                        }
                    }
                    assert (cycle.size() > 1) : "Cycle of less than two elements found";

                    newMoves.add(new Move(cycle.get(0).from, Allocation.getScratchRegister(), false));
                    for (Move move: cycle) {
                        newMoves.add(new Move(move.to, Allocation.getScratchRegister(), true));
                    }
                }
            }

            // Insert moves to basic block
            List<AbstractNode> moveNodes = new ArrayList<>();

            String[] ids = blockID.split(":");
            BasicBlock current = function.getBlockByID(Integer.parseInt(ids[0]));
            BasicBlock successor = function.getBlockByID(Integer.parseInt(ids[1]));
            for (Move move: newMoves) {
                assert (move.to.type != Allocation.Type.STACK &&
                        move.from.type != Allocation.Type.STACK) : "Move from/to non-registers are not implemented";
                if (move.isExchange)
                    moveNodes.add(new ExchangeNode(move.from, move.to));
                else
                    moveNodes.add(new MoveNode(move.from, move.to));
            }

            // add moves

            // if current node only has one successor
            if (current.getSuccessors().size() < 2) {
                AbstractNode lastNode = current.getNodes().get(current.getNodes().size()-1);
                if (lastNode instanceof BranchNode || lastNode instanceof FunctionCallNode)
                    current.getNodes().addAll(current.getNodes().size()-1, moveNodes);
                else
                    current.getNodes().addAll(current.getNodes().size(), moveNodes);
            } else {
                // Add two new basic blocks for move nodes
                BasicBlock bnode = new BasicBlock(current.parentFunction, "dummy");

                if (successor == current.fallThrough)
                    current.fallThrough = bnode;
                else if (successor == current.taken)
                    current.taken = bnode;
                else
                    assert true;

                successor.getPredecessors().remove(current);
                successor.getPredecessors().add(bnode);
                bnode.getPredecessors().add(current);
                current.successors.remove(successor);
                bnode.addSuccessor(successor);
                current.addSuccessor(bnode);
                bnode.getNodes().addAll(moveNodes);
                BranchNode bn = new BranchNode(BranchNode.Type.BRA, successor.getEntry());
                bn.fallThroughBlock = successor;
                bnode.addNode(bn);

                function.basicBlocks.add(bnode);
            }


        }


        System.out.println("********** End Moves **********");

        // Remove Phi nodes
        for (BasicBlock current: function.basicBlocks) {
            List<AbstractNode> found = new ArrayList<>();
            for (AbstractNode node: current.getNodes()) {
                if (node instanceof PhiFunctionNode)
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
                if (node.hasOutputVirtualRegister()) {
                    node.setAllocation(liveIntervalAllocationAt(intervalMap, node.getOutputVirtualReg(), node.sourceIndex));
                }

                for (AbstractNode child: node.getInputOperands()) {
                    if (child instanceof ImmediateNode)
                        child.setAllocation(new Allocation(Allocation.Type.IMMEDIATE, ((ImmediateNode) child).getValue()));
                    else
                        child.setAllocation(liveIntervalAllocationAt(intervalMap, child.getOutputVirtualReg(), node.sourceIndex));
                }
            }
        }
     }

    private HashMap<String, ArrayList<LiveInterval>> intervalMap;
    private int numPhysicalRegisters;
}