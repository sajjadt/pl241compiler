package org.pl241.ra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.pl241.ir.*;

class IntervalBuilder {
    public IntervalBuilder() {
		intervals = new HashMap<>();
	}

	public Map<String,LiveInterval> getLiveIntervals(){
		return intervals ;
	}
	
	public void build(List <BasicBlock> blocks){

	    System.out.println("Building live intervals");
		ListIterator li = blocks.listIterator(blocks.size());

		// Iterate in reverse.
		while (li.hasPrevious()) {
		    BasicBlock block =  (BasicBlock) li.previous();
            Set<String> live = new HashSet<>();
		    System.out.println("Processing block " + block);
		    
		    // Getting live info from successors
		    for (BasicBlock successor: block.getSuccessors()) {
		    	live.addAll(successor.getLiveIn());
		    }
		    
		    // Getting phi info from successors
		    for (BasicBlock successor:block.getSuccessors()) {
		    	for (AbstractNode node: successor.getNodes()) {
		    		if (node instanceof PhiFunctionNode) {
		    			if (((PhiFunctionNode)node).inputOf(block) != null) {
		    				live.add(((PhiFunctionNode)node).inputOf(block).getOutputVirtualReg());
		    				System.out.println("adding " + ((PhiFunctionNode)node).inputOf(block) + " to live set from succ phi function");
		    			}
		    		}
		    	}
		    }

		    // Set the live vars for the block
		    for (String var: live) {
		    	if (!intervals.containsKey(var)) {
		    		intervals.put(var, new LiveInterval(var, false));
		    	}
		    	intervals.get(var).addRange(block.bFrom, block.bTo);
                System.out.println("Add Range for the whole block " + block.bFrom + " to " + block.bTo + " to var " + var);
            }

		    // Process block operations in the reverse order
		    ListIterator ii = block.getNodes().listIterator(block.getNodes().size());
		    while (ii.hasPrevious()) {
			    AbstractNode node =  (AbstractNode) ii.previous();

			    if (!node.isExecutable())
	    			continue;

	    		if (node instanceof PhiFunctionNode) //Phi nodes will be handled in special way
	    		    continue;

			    if (node.hasOutputVirtualRegister()) {
			    	String opd = node.getOutputVirtualReg();
			    	//System.out.println("Removing " + opd + " from live");

			    	if(!intervals.containsKey(opd))
			    		intervals.put(opd, new LiveInterval(opd, false));

                    //System.out.println("Setting from for " + opd + " at " + node.getSourceIndex() );
			    	intervals.get(opd).setFrom(node.getSourceIndex());
			    	intervals.get(opd).definitionPoint = node.getSourceIndex();
			    	intervals.get(opd).addReference(node.getSourceIndex());
                    intervals.get(opd).addRange(node.getSourceIndex(), node.getSourceIndex());
			    	live.remove(opd);    	
			    }

			    if (!node.getInputOperands().isEmpty()) {
			    	List<AbstractNode> opds = node.getInputOperands();
			    	for(AbstractNode opd:opds) {
			    		if (!opd.hasOutputVirtualRegister()) {
                           // System.out.println("Ignoring  " + opd);
                            continue;
			    		}
				    	if (!intervals.containsKey(opd.getOutputVirtualReg()))
				    		intervals.put(opd.getOutputVirtualReg(), new LiveInterval(opd.getOutputVirtualReg(), false));

				    	intervals.get(opd.getOutputVirtualReg()).addRange(block.bFrom, node.getSourceIndex());
				    	intervals.get(opd.getOutputVirtualReg()).addReference(node.getSourceIndex());
				    	live.add(opd.getOutputVirtualReg());
				    	//System.out.println("Adding [" + opd.getOutputVirtualReg() + "] input operand  from [" + node.toString()+ "] node to live");
				    	//System.out.println("Range is " + block.bFrom + " to " + node.getSourceIndex());
			    	}
			    }
		    }
		    
		    // is done in above step
		    for (AbstractNode node: block.getNodes()) {
		        if (node instanceof PhiFunctionNode) {
		            if (live.contains(node.getOutputVirtualReg())) {
                        live.remove(node.getOutputVirtualReg());
                        intervals.get(node.getOutputVirtualReg()).definitionPoint = node.getSourceIndex();
                    }
                }
	    	}
		    
		    if (block.isLoopHeader()) {
                BasicBlock lastLoopBlock = null;
                for (BasicBlock pred: block.getPredecessors()) {
                    if (pred.dominators.contains(block)) {
                        assert lastLoopBlock == null;
                        lastLoopBlock = pred;
                    }
                }
                assert lastLoopBlock != null;
                for (String var: live){
                    intervals.get(var).addRange(block.bFrom, lastLoopBlock.bTo);
                }

		    }

		    block.liveIn = live ;
		    System.out.println( block.getIndex() + ".live-in:" +  block.liveIn.toString() );
		    System.out.println( block.getIndex() + "intervals:" +  intervals.toString() );
		}

	}

	public void printIntervals() {
		for(LiveInterval interval:intervals.values()){
			System.out.println(interval.varName + " " + interval.toString() );
		}
		
	}

    // Map from vars to live intervals of vars
    private Map<String,LiveInterval> intervals;
}
