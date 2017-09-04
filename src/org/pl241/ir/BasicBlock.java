package org.pl241.ir;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class BasicBlock {

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
		id = this.generateID("");
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
	    if (nodes.size() > 0)
		    return this.nodes.get(this.nodes.size() - 1);
	    else
	        return null;
	}

	public static void reset () {
        counter = 0;
        _sindex = 0;
        _lindex = 0;
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
		nodes.add(node);
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

		pw.print("BB" + _index + " [shape=record label=\"{");
        pw.print( "Block " + id + "\n");

		for (AbstractNode n: getNodes()) {
            if (n.isExecutable())
			    pw.print('|' + n.toString());

		}

		pw.print("}\" ] " + "\n");

		if (standalone) {
			pw.println("}");
			pw.close();
		}
	}


    public void toAllocDot(PrintWriter pw, boolean standalone) {
        if (standalone) {
            pw.println("digraph {");
            pw.println("rankdir=\"TD\"");
        }

        pw.print("BB" + _index + " [shape=record label=\"{");
        pw.print( "Block " + id + "\n");

        for (AbstractNode n: getNodes()) {
            if (n.isExecutable())
                pw.print('|' + n.toString());

        }

        pw.print("}\" ] " + "\n");

        if (standalone) {
            pw.println("}");
            pw.close();
        }
    }


	public void toDomTreeDot(PrintWriter pw) {

		pw.print("BB" + _index + " [shape=record label=\"{");
		boolean first = true;
		pw.println("BBL: " + _index);
		pw.print('|');
		for (AbstractNode n: getNodes()) {
			if(n instanceof LoadNode) {
				if (first)
					first = false;
				else
					pw.print('|');
				pw.print( n.toString() );
			}
			if (n.removed)
				pw.print('$');
		}
		pw.print("}\" ] " + "\n");
	}


	public boolean hasAssignmentTo(Variable var) {
		for (AbstractNode node: getNodes()) {
			if (node instanceof PhiNode )
				if (((PhiNode)node).variableName.equals(var.name))
					return true;

			if (node instanceof StoreNode)
				if (((StoreNode)node).memAddress.equals(var.name))
					return true;
		}
		return false;
	}


	public void addPhiNode(Variable var) {
		System.out.println("Phi function for var " + var.name + " added to BBL " + getIndex() );
		PhiNode phi = new PhiNode(var.name) ;
		if (this.lineIndex != null)
			phi.sourceIndex = this.lineIndex ;
		else {
		}
		nodes.add(0,phi);	
	}


	public String lastAssignment(Variable var) {
		ListIterator<AbstractNode> li = nodes.listIterator(nodes.size());
		while( li.hasPrevious() ){
			AbstractNode node = li.previous();
			if (node instanceof PhiNode)
				if (((PhiNode)node).variableName.equals(var.name))
					return node.nodeId;
            if (node instanceof StoreNode)
                if (((StoreNode)node).memAddress.equals(var.name))
                    return node.nodeId;
		}
		return "";
	}


	public AbstractNode getPhiOperand(String variableName, int bblIndex) {
        for (AbstractNode node: getNodes()) {
            if (node instanceof PhiNode)
                if (((PhiNode)node).variableName.equals(variableName))
                    return ((PhiNode) node).rightOperands.get(bblIndex);
        }
        return null;
    }

	public void addPhiOperand(Variable leftOperands, String lastAssignment, int bblIndex) {
		for (AbstractNode node: getNodes()) {
			if (node instanceof PhiNode) {
				if (((PhiNode)node).variableName.equals(leftOperands.name)) {
					((PhiNode)node).rightLabels.put(bblIndex , lastAssignment);
					System.out.println("Phi operand @ " + getIndex()  + "for var " + leftOperands.name + " "  + bblIndex + " " + lastAssignment) ;
				}
			}
		}
	}
	
	public void rename(){
		// Foreache phi block
		for( AbstractNode node: getNodes() ){
			if (node instanceof PhiNode ){
				String name = ((PhiNode)node).originalVarName;
				System.out.println("pushing " + name );
				String newName = Variable.generateNewName( name ) ;
				((PhiNode)node).variableName = newName ;
			}
		}
		for( AbstractNode node: getNodes()) {
			if (node instanceof LoadNode) {
				String src =((LoadNode)node).variableId;
				String address = Variable.getTopmostName(src);
				((LoadNode)node).variableId =  address;
			}

            if (node instanceof StoreNode) {
                String name = ((StoreNode)node).originalMemAddress;
                System.out.println("pushing " + name );
                String newName = Variable.generateNewName( name );
                ((StoreNode)node).memAddress = newName ;
            }
			
			if (node instanceof IONode && ((IONode)node).writeData()) {
				AbstractNode src =((IONode)node).getOperandAtIndex(0);
				//String address = Variable.getTopmostName(src);
				//saji TODO new
				// ((IONode)node).operands.set(0, new LabelNode(address));
			}
		}
		
		for( BasicBlock b: getSuccessors() ){
			for( AbstractNode node: b.getNodes()){
				if (node instanceof PhiNode ){
					//for( Map.Entry<String, String> entry: ((PhiNode)node).operands.entrySet() ){
					//	entry.setSrcOperand( Variable.getTopmostName(entry.getValue()) );
						
					//}
					String name = ((PhiNode)node).originalVarName;
					System.out.println("Reading " + name );
					String newName = Variable.getTopmostName(name) ;
					System.out.println(newName+" from BBL " + getIndex() ) ;
					((PhiNode)node).rightOperands.put( getIndex() , new LabelNode(newName) ) ;
				}
			}
		}

		for( BasicBlock b: immediateDominants ) { 
			//TODO better way to point to D.Tree
			if(  b.getIndex() != this._index ){
				System.out.println("Renaming block " + b._index );
				b.rename();
			}
		}

		for (AbstractNode node: getNodes()) {
			if (node instanceof PhiNode ) {
				String name = ((PhiNode)node).originalVarName;
				Variable.popTopmostName(name);
			}
		}
	}

	public int indexIR(int index) {
	    System.out.println("Indexing Basic block " + this.id + " from index " + index);
        bFrom = index;
		for (AbstractNode node: getNodes()) {
			if (node.isExecutable()) {
				node.sourceIndex = index;
				index += 2; // Makes room for spills
			}
		}
		bTo = index; // For potential branch
        return bTo;
	}

    @Override
    public String toString() {
        return tag;
    }



	public String id;
	private static int counter = 0 ;
	public static String generateID (String str) {
		return str + counter++;
	}

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
}
