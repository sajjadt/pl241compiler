package org.pl241.ir;

import org.pl241.Function;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class BasicBlock {
	public static  int _sindex = 0;
	public static  int _lindex = 0;
	private int _index;
	private List<AbstractNode> nodes;
	public ArrayList<BasicBlock> successors;
	public ArrayList<BasicBlock> predecessors;
	public HashSet<BasicBlock> dominators; // Blocks that are dominating this
	public HashSet<BasicBlock> dominatorsTemp;  
	public HashSet<BasicBlock> immediateDominants;
	public HashSet<BasicBlock> dominants; // Blocks this one is dominating
	public HashSet<BasicBlock> dominatorFrontiers;
	public Function parentFunction;
	
	
	public boolean loopHeader ;
	public boolean loopBody ;
	public Set<String> liveIn ;
	
	public int bFrom ;
	public int bTo ;
	
	private Integer lineIndex; // a lineindex used for phi nodes
	
	public BasicBlock(Function _parentFunction, String _tag){
		nodes = new ArrayList<AbstractNode>();
		successors = new ArrayList<BasicBlock>();
		predecessors = new ArrayList<BasicBlock>();
		dominators = new HashSet<BasicBlock>();
		dominatorsTemp = new HashSet<BasicBlock>();
		dominants = new HashSet<BasicBlock>();
		dominatorFrontiers = new HashSet<BasicBlock>();
		immediateDominants = new HashSet<BasicBlock>();
		parentFunction  = _parentFunction;
		tag = _tag;
		_index = ++_sindex;
		
		loopBody = false; 
		loopHeader = false ;
		
		liveIn = new HashSet<String>();
	}
	
	
	public AbstractNode getEntry(){
		for(AbstractNode node:nodes ){
			if ( node instanceof LoadNode || node instanceof ImmediateNode ){
				continue;
			}
			return node;
		}
		return null ;
	}

	public AbstractNode getLastNode() {
		return this.nodes.get(this.nodes.size() - 1);
	}

	// Return list of live variables
	public Set<String> getLiveIn(){
		return liveIn;
	}
	public void addSuccessor(BasicBlock succ) {
		if (succ == null)
			throw new NullPointerException("Setting successor of basic block to null");
		successors.add(succ);
	}
	
	public void addDominator(BasicBlock succ) {
		if (succ == null)
			throw new NullPointerException("Setting dominator of basic block to null");
		dominators.add(succ);
	}
	
	
	public void removeDominator(BasicBlock succ) throws AnalysisException {
		if (succ == null)
			throw new NullPointerException("Trying to remove a null dominator");
		if (!dominators.contains(succ))
			throw new AnalysisException("The basic block is not a dominator");
		dominators.remove(succ);
	}
	
	/**
	 * Remove a successor.
	 * @throws AnalysisException 
	 */
	public void removeSuccessor(BasicBlock succ) throws AnalysisException {
		if (succ == null)
			throw new NullPointerException("Trying to remove a null successor");
		if (!successors.contains(succ))
			throw new AnalysisException("The basic block is not a successor");
		successors.remove(succ);
	}

	
	
	public HashSet<BasicBlock> getDominators() {
		return dominators;
	}
	/**
	 * Adds a Predecessor.
	 */
	public void addPredecessor(BasicBlock succ) {
		if (succ == null)
			throw new NullPointerException("Setting predecessor of basic block to null");
		predecessors.add(succ);
	}
	
	/**
	 * Remove a Predecessor.
	 * @throws AnalysisException 
	 */
	public void removePredecessor(BasicBlock succ) throws AnalysisException {
		if (succ == null)
			throw new NullPointerException("Trying to remove a null predecessor");
		if (!predecessors.contains(succ))
			throw new AnalysisException("The basic block is not a predecessor");
		predecessors.remove(succ);
	}

	/**
	 * Returns the Predecessors of this block.
	 */
	public ArrayList<BasicBlock> getPredecessors() {
		return predecessors;
	}
	
	public int getIndex(){
		return _index;
	}
	
	public void addNode( AbstractNode node ){
		//if( node  instanceof FetchNode == false ){
			//if( sourceLocation == null ){
			//	sourceLocation = ++_lindex ;
			// bFrom = sourceLocation ;
			// }
			//node.sourceLocation = ++_lindex;
			//bTo = node.sourceLocation ;
		//}
		
		nodes.add(node);
		parentFunction.irMap.put(node.uniqueLabel, node);
	}
	
	public List<AbstractNode> getNodes() {
		return nodes;
	}
	
	public String tag;
	
	
	public ArrayList<BasicBlock> getSuccessors(){
		return successors;
	}
	
	
	public BasicBlock taken ;
	public BasicBlock fallThrough ;
	public BasicBlock parent;
	
	public void toDot(PrintWriter pw, boolean standalone) {
		if (standalone) {
			pw.println("digraph {");
			pw.println("rankdir=\"TD\"");
		}
		pw.print("BB" + _index + " [shape=record uniqueLabel=\"{");
		pw.print( "BBL Range: " + bFrom + ":" + bTo + "\n|");
		pw.print( "TAG: " + tag + "\n|");
		boolean first = true;
		for (AbstractNode n : getNodes()) {
			if (first)
				first = false;
			else
				pw.print('|');
			pw.print( n.toString() );
		//	if (n.isDead()) 
		//		pw.print('$');
		}
		pw.print("}\" ] " + "\n");
		if (standalone) {
			pw.println("}");
			pw.close();
		}
	}

	public void toDomTreeDot(PrintWriter pw) {
		pw.print("BB" + _index + " [shape=record uniqueLabel=\"{");
		boolean first = true;
		pw.println("BBL: " + _index );
		pw.print('|');
		for (AbstractNode n : getNodes()) {
			if( n instanceof MoveNode || n instanceof LoadNode ){
				if (first)
					first = false;
				else
					pw.print('|');
				pw.print( n.toString() );
			}
			
		//	if (n.isDead()) 
		//		pw.print('$');
		}
		pw.print("}\" ] " + "\n");
	}


	public boolean hasAssignmentTo(Variable var) {
		// TODO Auto-generated method stub
		for( AbstractNode node: getNodes()){
			if (node instanceof MoveNode ){
				if(  ((MoveNode)node).memAddress.equals(var.name) ){
					return true;
				}
			}
			if (node instanceof PhiNode ){
				if(  ((PhiNode)node).memAddress.equals(var.name) ){
					return true;
				}
			}
		}
		return false;
	}


	public void addPhiNode(Variable var) {
		System.out.println("Phi function for var " + var.name + " added to BBL " + getIndex() );
		PhiNode phi = new PhiNode(var.name) ;
		if( this.lineIndex != null )
			phi.sourceLocation = this.lineIndex ;
		else {
			// TODO lineindex for empty block 
			
		}
		nodes.add(0,phi);	
	}


	public String lastAssignment(Variable var) {
		// TODO Auto-generated method stub
		ListIterator<AbstractNode> li = nodes.listIterator(nodes.size());
		while( li.hasPrevious() ){
			AbstractNode node = li.previous();
			if (node instanceof MoveNode ){
				if(  ((MoveNode)node).memAddress.equals(var.name) ){
					return node.uniqueLabel;
				}
			}
			if (node instanceof PhiNode ){
				if(  ((PhiNode)node).memAddress.equals(var.name) ){
					return node.uniqueLabel;
				}
			}
		}
		return "";
	}


	public void addPhiOperand(Variable leftOperands, String lastAssignment, int bblIndex) {
		for( AbstractNode node: getNodes()){
			if (node instanceof PhiNode ){
				if(  ((PhiNode)node).memAddress.equals(leftOperands.name) ){
					((PhiNode)node).rightLabels.put(bblIndex , lastAssignment ) ;
					System.out.println("Phi operand @ " + getIndex()  + "for var " + leftOperands.name + " "  + bblIndex + " " + lastAssignment) ;
					
				}
			}
		}
	}
	
	public void rename(){
		// Foreache phi block
		for( AbstractNode node: getNodes() ){
			if (node instanceof PhiNode ){
				String name = ((PhiNode)node).originalMemAddress;
				System.out.println("pushing " + name );
				String newName = Variable.generateName( name ) ;
				((PhiNode)node).memAddress = newName ;
			}
		}
		for( AbstractNode node: getNodes()){
			if (node instanceof MoveNode ){
				String name = ((MoveNode)node).originalMemAddress;
				System.out.println("pushing " + name );
				String newName = Variable.generateName( name ) ;
				((MoveNode)node).memAddress = newName ;
				
			}
			
			if (node instanceof LoadNode ){
				String src =((LoadNode)node).memAddress  ;
				String address = Variable.getTopmostName(src);
				((LoadNode)node).memAddress =  address ;
			}
			
			if (node instanceof IONode && ((IONode)node).writeData() ){
				AbstractNode src =((IONode)node).getOperandAtIndex(0)  ;
				//String address = Variable.getTopmostName(src);
				//saji TODO new
				// ((IONode)node).operands.set(0, new LabelNode(address));
			}
			
		}
		
		for( BasicBlock b: getSuccessors() ){
			for( AbstractNode node: b.getNodes()){
				if (node instanceof PhiNode ){
					//for( Map.Entry<String, String> entry: ((PhiNode)node).operands.entrySet() ){
					//	entry.setValue( Variable.getTopmostName(entry.getValue()) );
						
					//}
					String name = ((PhiNode)node).originalMemAddress ;
					System.out.println("Reading " + name );
					String newName = Variable.getTopmostName(name) ;
					System.out.println(newName+" from BBL " + getIndex() ) ;
					((PhiNode)node).rightOperands.put( getIndex() , new LabelNode(newName) ) ;
				}
			}
		}
		//for( BasicBlock b: parentFunction.basicBlocks ) {
		//	//TODO better way to point to D.Tree
			
		//	if( b.immediateDominants.contains(this) && b.getIndex() != this._index ){
		//		System.out.println("Renaming block " + b._index );
		//		b.rename();
		//	}
		//}
		
		for( BasicBlock b: immediateDominants ) { 
			//TODO better way to point to D.Tree
			
			if(  b.getIndex() != this._index ){
				System.out.println("Renaming block " + b._index );
				b.rename();
			}
		}
		
		
		for( AbstractNode node: getNodes()){
			if (node instanceof PhiNode  ){
				String name = ((PhiNode)node).originalMemAddress ;
				Variable.popTopmostName(name);
				System.out.println("popping " + name );
			}
			if (node instanceof MoveNode  ){
				String name = ((MoveNode)node).originalMemAddress ;
				Variable.popTopmostName(name);
				System.out.println("popping " + name );
			}
		}
	}


	public void indexIR() {
		// index itself
		lineIndex = ++_lindex; // For potential phis
		bFrom = lineIndex ;
		bTo = lineIndex;
		for(AbstractNode node: getNodes() ){
			if( node instanceof PhiNode ){
				node.sourceLocation = lineIndex ;
			}
			else if( node  instanceof LoadNode == false && node  instanceof ImmediateNode == false ){
				node.sourceLocation = ++_lindex ;
				bTo = node.sourceLocation;
			}
		}
		bTo =  ++_lindex; // For potential branch
	}	
}
