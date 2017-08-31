package org.pl241;

import org.pl241.ir.*;
import org.pl241.ra.Allocation;
import org.pl241.ra.BuildIntervals;
import org.pl241.ra.LiveInterval;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


public class Function  {


    public Function(){
        basicBlocks = new ArrayList<>();
        allocationMap = new HashMap<>();
        symbolTable = new VarInfoTable();
        _index = _sindex++;
        irMap = new HashMap<>();
        spillIndex = 0 ;
    }

    // Unique across program
	public String name;

	public static int _sindex = 0;
	private int _index;

	public VarInfoTable symbolTable;

	public List<BasicBlock> basicBlocks;

	// TODO: remove irMap as it seems redundant
	public Map<String,AbstractNode> irMap;
	public Map<String, Allocation> allocationMap;

	public BasicBlock getEntryBlock() {
		return basicBlocks.get(0);
	}

	private SourceLocation location;
	public SourceLocation getSourceLocation(){
		return location;
	}
	
	public String getName(){
		return name ;
	}
	

	
	public Map<String, LiveInterval> getliveIntervals(){
		BuildIntervals bin = new BuildIntervals();
        
        	List<BasicBlock> blocks = getBlocksInLayoutOrder();
            bin.build(blocks);
            for(BasicBlock b: blocks){
            	System.out.println(b.getIndex() + ":" + b.liveIn.toString() );
            }
            System.out.println("LEVEL 1 DEAD VARIABLES");
            HashMap<String,LiveInterval> intervals = (HashMap<String, LiveInterval>) bin.getLiveIntervals() ;
            for( LiveInterval interval: intervals.values() ){
            	if( interval.getRanges().isEmpty() ){
            		System.out.print(interval.varName+",");
            	} 
            }
            System.out.println();
            System.out.println("LIVE VARIABLE RANGES");
            for( LiveInterval interval: intervals.values() ){
            	if(! interval.getRanges().isEmpty() ){
            		System.out.println(interval.varName+":" + interval.toString());
            	} 
            }
       
        return bin.getLiveIntervals();
	}

	
	public void setBranchTargets(){
	    String target = null;
		for( BasicBlock b: this.getBasicBlocks() ){
			for ( AbstractNode node: b.getNodes() ){
				if ( node instanceof BranchNode) { //TODO uncoditioned as well
					// rewrite address
					if (!((BranchNode)node).isCall) {
                        ((BranchNode)node).takenTarget = (b.getSuccessors().get(0)).getEntry().nodeId;
                        ((BranchNode)node).takenBlock = (b.getSuccessors().get(0));
                        if (b.getSuccessors().size() > 1) {
                            ((BranchNode) node).nonTakenTarget = (b.getSuccessors().get(1)).getEntry().nodeId;
                            ((BranchNode) node).nonTakenBlock = (b.getSuccessors().get(1));
                        }
                    }
				}
			}
		}
	}
	
	
	public void toDot(PrintWriter pw, boolean standalone, boolean main) {
//      List<String> colors = Collections.newList();
//      colors.add("black");
//      colors.add("red1");
//      colors.add("green1");
//      colors.add("blue1");
		if (standalone) {
			//pw.println("digraph {");
		} else {
			pw.println("subgraph " + "cluster" + _index + " {");
			//pw.println("nodeId=\"" + (main ? "<main> " : "") + toString() + "\\n" + location
            //      + (outerFunction != null ? "\\nouter: " + (outerFunction.getName() == null ? "<main>" : outerFunction.getName()) : "")  +  "\";");
			pw.println("label=" + getName() );
			pw.println("labelloc=\"t\";");
			pw.println("fontsize=18;");
		}
		pw.println("rankdir=\"TD\"");
		Set<BasicBlock> labels = new HashSet<>();
		pw.println("BB_entry" + _index + "[shape=none,label=\"\"];");
		pw.println("BB_entry" + _index + " -> BB" + getEntryBlock().getIndex()
				+ " [tailport=s, headport=n, headlabel=\"    " + getEntryBlock().getIndex() + "\"]");
		labels.add(getEntryBlock());
		for (BasicBlock b : basicBlocks) {
			b.toDot(pw, false);
//	        int color_index = 0;
			for (BasicBlock bs : b.getSuccessors()) {
//              String color = colors.get(color_index);
//              color_index = (color_index + 1) % colors.size();
				pw.print("BB" + b.getIndex() + " -> BB" + bs.getIndex() 
						+ " [tailport=s, headport=n");
//				pw.print(", color=" + color);
				if (!labels.contains(bs)) {
					labels.add(bs);
					pw.print(", headlabel=\"      " + bs.getIndex() /*+ (bs.hasOrder() ? "[" +bs.getOrder() + "]" : "")*/ +"\"");
				}
				pw.println("]");
			}
			//BasicBlock ex = b.getExceptionHandler();
			/////if (ex != null && b.canThrowExceptions()) {
				// if (ex != null && ex != b) {
			////	pw.print("BB" + b.getIndex() + " -> BB" + ex.getIndex() 
			//			+ " [tailport=s, headport=n, color=gray");
			//	if (!labels.contains(ex)) {
			//		labels.add(ex);
			//		pw.print(", headlabel=\"      " + ex.getIndex() +"\"");
			//	}
			//	pw.println("]");
			//}
		}
		pw.println("}");
		if (standalone) {
			pw.flush();
		}
	}
	
	public void domToDot(PrintWriter pw, boolean standalone, boolean main) {
//      List<String> colors = Collections.newList();
//      colors.add("black");
//      colors.add("red1");
//      colors.add("green1");
//      colors.add("blue1");
		if (standalone) {
			//pw.println("digraph {");
		} else {
			pw.println("subgraph " + "cluster" + _index + " {");
			//pw.println("nodeId=\"" + (main ? "<main> " : "") + toString() + "\\n" + location
            //      + (outerFunction != null ? "\\nouter: " + (outerFunction.getName() == null ? "<main>" : outerFunction.getName()) : "")  +  "\";");
			pw.println("label=" + getName() );
			pw.println("labelloc=\"t\";");
			pw.println("fontsize=18;");
		}
		pw.println("rankdir=\"TD\"");
		Set<BasicBlock> labels = new HashSet<>();
		pw.println("BB_entry" + _index + "[shape=none,label=\"\"];");
		pw.println("BB_entry" + _index + " -> BB" + getEntryBlock().getIndex()
				+ " [tailport=s, headport=n, headlabel=\"    " + getEntryBlock().getIndex() + "\"]");
		labels.add(getEntryBlock());
		for (BasicBlock b : basicBlocks) {
			b.toDomTreeDot(pw);
//	        int color_index = 0;
			for (BasicBlock bs : b.immediateDominants ) {
				if ( bs.getIndex() ==  b.getIndex() )
					continue;
				
//              String color = colors.get(color_index);
//              color_index = (color_index + 1) % colors.size();
				pw.print("BB" + b.getIndex() + " -> BB" + bs.getIndex() 
						+ " [tailport=s, headport=n");
//				pw.print(", color=" + color);
				if (!labels.contains(bs)) {
					labels.add(bs);
					pw.print(", headlabel=\"      " + bs.getIndex() /*+ (bs.hasOrder() ? "[" +bs.getOrder() + "]" : "")*/ +"\"");
				}
				pw.println("]");
			}
			//BasicBlock ex = b.getExceptionHandler();
			/////if (ex != null && b.canThrowExceptions()) {
				// if (ex != null && ex != b) {
			////	pw.print("BB" + b.getIndex() + " -> BB" + ex.getIndex() 
			//			+ " [tailport=s, headport=n, color=gray");
			//	if (!labels.contains(ex)) {
			//		labels.add(ex);
			//		pw.print(", headlabel=\"      " + ex.getIndex() +"\"");
			//	}
			//	pw.println("]");
			//}
		}
		pw.println("}");
		if (standalone) {
			pw.flush();
		}
	}
	
	
	private void setPredecessors(){
		 for (BasicBlock i : getBasicBlocks()) {
			 for (BasicBlock j : i.getSuccessors()) {
					 j.addPredecessor(i);
			 }
		 }
	}
	
	private List<BasicBlock> getBasicBlocks() {
		return basicBlocks;
	}

	public void computeDominateDependance(){
		setPredecessors(); //For all basic basicBlocks within the function
		
		for( BasicBlock i : getBasicBlocks()){ // Starting set of dominators
			if ( i== getEntryBlock() ){
				i.addDominator(i);
				i.dominants.add(i);
			}
			else {
				for( BasicBlock j : getBasicBlocks()){
					//if ( j != getEntryBlock()){
						i.addDominator(j);
						j.dominants.add(i);
					//}
				}
			}
			
		}
		boolean change = true; 
		while(change){
			// Change this with dfs
			change = false ;
			for( BasicBlock j : getBasicBlocks()){
				if ( j != getEntryBlock()){
					for( BasicBlock i : j.getPredecessors()){
						 j.getDominators().remove(j);
						 j.dominants.remove(j);
						 if ( j.getDominators().retainAll(i.getDominators()) ){
							 
							 change = true;
						 }
						j.getDominators().add(j);
						j.dominants.add(j);
					}
				}
				
					
			}
		}
	}
	
	public void computeDominatorTree() throws AnalysisException{
		//TODO clean the map mess
		//TODO is screwing dominance information
		for( BasicBlock b: getBasicBlocks()) {
			b.dominatorsTemp.addAll(b.dominators);
		}
		BasicBlock entryBlock = getEntryBlock() ;
		HashSet<BasicBlock> setbl = new HashSet<BasicBlock>();
		Stack<BasicBlock> sbl = new Stack<BasicBlock>();
		sbl.push(entryBlock);
		setbl.add(entryBlock);
		while(! sbl.isEmpty() ){
			BasicBlock bl = sbl.pop();
			for( BasicBlock i : getBasicBlocks()){ // Starting set of dominators
				if ( i.dominators.contains(bl)){
					i.removeDominator(bl);
					
				}
			}
			for( BasicBlock i : getBasicBlocks()){
				
				if( i.dominators.size() == 1 && i.dominators.contains(i) && setbl.contains(i) == false ){
					System.out.println("pushing " + i.getIndex() );
					sbl.push(i);
					//i.immediateDominants.add(bl);
					bl.immediateDominants.add(i);
					setbl.add(i);
				}
				
			}
		}
		for( BasicBlock b: getBasicBlocks()) {
			b.dominators.clear();
			b.dominators.addAll(b.dominatorsTemp);
			b.dominatorsTemp.clear();
		}
		
	}
	public void computeDominatorFrontiers(){
		for( BasicBlock b : getBasicBlocks()){
			for( BasicBlock d: b.getDominators() ){
				//if( d.getIndex() == b.getIndex() )
				//	continue;
				for( BasicBlock f: b.getSuccessors() ){
					if(  b.dominators.contains(d)  )
					{
						if( ! f.dominators.contains(d) || d.getIndex() == f.getIndex() )
							d.dominatorFrontiers.add(f);
					}
						
				}
				
			}
					 
		}
	}
	
	public void insertPhiFunction(){
		for( Variable var : symbolTable.getVars() ){
			System.out.println("Checking var " + var.name );
			Set<BasicBlock> workList = new HashSet<BasicBlock>();
			Set<BasicBlock> everOnWorkList =  new HashSet<BasicBlock>() ;
			Set<BasicBlock> alreadyHasPhiFunc =  new HashSet<BasicBlock>() ;
			for ( BasicBlock b: getBasicBlocks() ){
				if( b.hasAssignmentTo(var)){
					workList.add(b);
				}
			}
			everOnWorkList.addAll(workList);
			while(workList.size() > 0 ){
				Iterator<BasicBlock> iterable = workList.iterator();
				BasicBlock node = iterable.next();
				iterable.remove();
				for( BasicBlock df: node.dominatorFrontiers ){
					if (! alreadyHasPhiFunc.contains(df)){
						df.addPhiNode(var);
						alreadyHasPhiFunc.add(df);
						if (! everOnWorkList.contains(df)){
							workList.add(df);
							everOnWorkList.add(df);
						}
					}
					df.addPhiOperand(var,node.lastAssignment(var), node.getIndex() );
				}
			}
					
		}
	}
	
	public void rename() {
		getEntryBlock().rename();
	}

	public void indexIR() {
		List<BasicBlock> blocks = getBlocksInLayoutOrder();
		for (BasicBlock b : blocks) {
			b.indexIR(); 
		}
	}

	public List<BasicBlock> getBlocksInLayoutOrder() {
		List<BasicBlock> blocks = new ArrayList<>();
		Stack<BasicBlock> stack = new Stack<>();
		// Pre-order traversal of Dominator tree
		stack.push(getEntryBlock());
		while( ! stack.isEmpty() ){
			BasicBlock block = stack.pop();
			blocks.add(block);
			
			// Then others
			for(BasicBlock b : block.immediateDominants ){
				if( b != block.fallThrough && b  != block.taken ){
					stack.push(b);
				}
			}
			// Then if
			for(BasicBlock b : block.immediateDominants ){
				if( b == block.taken ){
					stack.push(b);
					break;
				}
			}
			// First fallthrough 
			for(BasicBlock b : block.immediateDominants ){
				if( b == block.fallThrough ){
					stack.push(b);
					break;
				}
			}
		}
		return blocks;
	}

	// After indexing, need to set jump targets or add another jump at the end of basic basicBlocks due to indexing
	public void addMissingBranches() {
		for (BasicBlock block: getBasicBlocks()) {
			if (block.successors.size() == 1 && !(block.getLastNode() instanceof  BranchNode)) {
                AbstractNode entry = block.successors.get(0).getEntry() ;
                if( entry != null )
                {
                    BranchNode node = new BranchNode();
                    node.takenTarget =  entry.nodeId;
                    node.sourceLocation = block.bTo ;
                    block.addNode(node);
                } else {
                    // TODO empty basicblock
                }
			}
		}		
	}

	public void resolve(Map<String,LiveInterval> intervals) {
		Allocation moveFrom = null;
		Allocation moveTo= null ;
		Map<Allocation,Allocation> mapping = new HashMap<Allocation,Allocation>();
		for(BasicBlock pred: basicBlocks){
			for(BasicBlock succ:pred.getSuccessors()){
				for( LiveInterval interval: intervals.values()){
					if( interval.isAlive(succ.bFrom)){
						if( interval.start() == succ.bFrom ){
							// Find the phi function defining it
							for(AbstractNode node:succ.getNodes()){
								if( node instanceof PhiNode && ((PhiNode)node).memAddress == interval.varName ){
									AbstractNode operand = ((PhiNode)node).inputOf(pred);
									// TODO if operand is constant
									moveFrom = intervals.get(operand).allocatedLocation ; // TODO location of opd at end of the pred
								}
							}
						} else{
							moveFrom = interval.allocatedLocation ; // TODO location of it.var at end of the pred
						}
						moveTo =  interval.allocatedLocation ; // TODO location of it.var at beginning of succ
						if ( ! moveFrom.equals(moveTo) ){
							mapping.put(moveFrom, moveTo);
						}
					}
				}
			}
		}
	}
	
	private int spillIndex = 0 ; 
	public int getNextSpillIndex(){
		return spillIndex++;
	}
	
	public List<AbstractNode> getExecutableStream(){
        List<AbstractNode> executableItems = new ArrayList<>();
	    List<BasicBlock> orderedBlocks = this.getBlocksInLayoutOrder();
        for(BasicBlock block:orderedBlocks){
            executableItems.addAll(block.getNodes());
        }
		return executableItems ;
	}
}
	
	

	

