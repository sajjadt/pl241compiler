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

public class BuildIntervals {
    // Map from vars to live intervals of vars
	private Map<String,LiveInterval> intervals;
	public BuildIntervals(){
		intervals = new HashMap<String, LiveInterval>();
	}
	public Map<String,LiveInterval> getLiveIntervals(){
		return intervals ;
	}
	
	public void build(List <BasicBlock> blocks){
		/*
		ListIterator li = blocks.listIterator(blocks.size());

		// Iterate in reverse.
		while (li.hasPrevious()) {
		    BasicBlock block =  (BasicBlock) li.previous();
		    
		    System.out.println("Processin block " + block.getIndex() );
		    
		    // Getting live info from successors
		    Set<String> live = new HashSet<String>();
		    for(BasicBlock successor:block.getSuccessors() ){
		    	live.addAll(successor.getLiveIn());
		    }
		    
		    // Getting phi info from successors
		    for(BasicBlock successor:block.getSuccessors() ){
		    	for( AbstractNode node: successor.getNodes() ){
		    		if( node instanceof PhiNode ){
		    			if (((PhiNode)node).inputOf(block) != null) {
		    				// TODO new live.add(  ((PhiNode)node).inputOf(block) );
		    				System.out.println("Phi node: adding " + ((PhiNode)node).inputOf(block));
		    			}
		    		}
		    	}
		    }
		    
		    
		    // Set the live vars for the complete block
		    for( String var: live ){
		    	if( !  intervals.containsKey(var) ){
		    		intervals.put(var, new LiveInterval(var));
		    	}
		    	intervals.get(var).addRange(block.bFrom, block.bTo);
		    }
		    
		    
		    // Operations in reverse order
		    ListIterator ii = block.getNodes().listIterator(block.getNodes().size());
		    while( ii.hasPrevious() ) {
			    AbstractNode node =  (AbstractNode) ii.previous();  /// TODO ignoring phi node according to Franz paper


			    if( node != null && ( node instanceof LoadNode || node instanceof ImmediateNode || node instanceof PhiNode ) ){
	    			//System.out.println("Ignoring " + node.toString() );
	    			continue ;
	    		}
			    //if( node != null && ( node instanceof IONode && ((IONode)node). ) ){
	    		//	System.out.println("Ignoring " + node.toString() );
	    		//	continue ;
	    		//}
			    
			    
			    if( node.getOutputOperand() != null ){  
			    	String opd = node.getOutputOperand();
			    	System.out.println("Removing " + opd + " @ output side");
			    	if( !  intervals.containsKey(opd) ){
			    		intervals.put(opd, new LiveInterval(opd));
			    	}
			    	intervals.get(opd).setFrom(node.getSourceLocation());
			    	intervals.get(opd).definitionPoint = node.getSourceLocation();
			    	intervals.get(opd).addReference(node.getSourceLocation()); // TODO write into counts as a reference?
			    	live.remove(opd);    	
			    }
			    if(  ! node.getInputOperands().isEmpty() ){  
			    	List<AbstractNode> opds = node.getInputOperands();
			    	for(AbstractNode opd:opds ){
			    		//AbstractNode anode = block.parentFunction.irMap.get(opd);
			    		if( opd != null ){
			    			if ( opd instanceof LoadNode ){
			    				//TODO new
								// opd = ((LoadNode)anode).memAddress;
			    			} else if (opd instanceof ImmediateNode) {
			    				System.out.println("Ignoring imm " + opd + " @ input side");
				    			continue ;
			    			}
			    			
			    		}
				    	if (!intervals.containsKey(opd.nodeId)) {
				    		intervals.put(opd.nodeId, new LiveInterval(opd.nodeId));
				    	}
				    	intervals.get(opd.nodeId).addRange(block.bFrom, node.getSourceLocation());
				    	intervals.get(opd.nodeId).addReference(node.getSourceLocation());
				    	// TODO saji live.add(opd);
				    	System.out.println("Adding " + opd + " @ input side from " + node.toString());
			    	}
			    }
			    
		    }
		    
		    // is done in above step
		    for( AbstractNode node: block.getNodes() ){
		    if( node instanceof PhiNode ){    	
	    			live.remove(node.getOutputOperand());
	    			intervals.get(node.getOutputOperand()).definitionPoint = node.getSourceLocation();
	    		}
	    	}
		    
		    if( block.loopHeader ){
		    	// TODO last block of the loop body?
		    }
		    block.liveIn = live ;
		    System.out.println( block.getIndex() + ":" +  block.liveIn.toString() );
		    System.out.println( block.getIndex() + ":" +  intervals.toString() );	
		}
		*/
	}

	public void printIntervals() {
		for(LiveInterval interval:intervals.values()){
			System.out.println(interval.varName + " " + interval.toString() );
		}
		
	}
	
}
