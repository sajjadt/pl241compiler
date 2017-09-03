package org.pl241.ra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.pl241.ir.AbstractNode;
import org.pl241.ir.BasicBlock;
import org.pl241.ir.LoadNode;
import org.pl241.ir.ImmediateNode;
import org.pl241.ir.PhiNode;

public class IntervalBuilder {
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
		    		if (node instanceof PhiNode) {
		    			if (((PhiNode)node).inputOf(block) != null) {
		    				live.add(((PhiNode)node).inputOf(block).getOutputOperand());
		    				System.out.println("Phi node: adding " + ((PhiNode)node).inputOf(block));
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
		    }

		    // Process block operations in the reverse order
		    ListIterator ii = block.getNodes().listIterator(block.getNodes().size());
		    while (ii.hasPrevious()) {
			    AbstractNode node =  (AbstractNode) ii.previous();

			    if (!node.isExecutable())
	    			continue ;

	    		if (node instanceof PhiNode) //Phi nodes will be handled in special way
	    		    continue;

			    if (node.getOutputOperand() != null) {
			    	String opd = node.getOutputOperand();
			    	System.out.println("Removing " + opd + " @ output side");
			    	if( !  intervals.containsKey(opd) ){
			    		intervals.put(opd, new LiveInterval(opd, false));
			    	}
			    	intervals.get(opd).setFrom(node.getSourceIndex());
			    	intervals.get(opd).definitionPoint = node.getSourceIndex();
			    	intervals.get(opd).addReference(node.getSourceIndex()); // TODO write into counts as a reference?
			    	live.remove(opd);    	
			    }

			    if (!node.getInputOperands().isEmpty()) {
			    	List<AbstractNode> opds = node.getInputOperands();
			    	for(AbstractNode opd:opds) {
			    		if (opd instanceof ImmediateNode) {
                            System.out.println("Ignoring imm " + opd + " @ input side");
                            continue;
			    		}
				    	if (!intervals.containsKey(opd.getOutputOperand())) {
				    		intervals.put(opd.getOutputOperand(), new LiveInterval(opd.getOutputOperand(), false));
				    	}
				    	System.out.println(opd.getOutputOperand());
				    	intervals.get(opd.getOutputOperand()).addRange(block.bFrom, node.getSourceIndex());
				    	intervals.get(opd.getOutputOperand()).addReference(node.getSourceIndex());
				    	live.add(opd.getOutputOperand());
				    	System.out.println("Adding [" + opd.getOutputOperand() + "] input operand  from [" + node.toString()+ "] node");
			    	}
			    }
			    
		    }
		    
		    // is done in above step
		    for (AbstractNode node: block.getNodes()) {
		        if (node instanceof PhiNode) {
	    			live.remove(node.getOutputOperand());
	    			intervals.get(node.getOutputOperand()).definitionPoint = node.getSourceIndex();
	    		}
	    	}
		    
		    if (block.loopHeader) {
		    	// TODO last block of the loop body?
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
