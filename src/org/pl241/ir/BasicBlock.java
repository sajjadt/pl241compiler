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
		dominatorFrontiers = new HashSet<BasicBlock>();
		immediateDominants = new HashSet<BasicBlock>();
		parentFunction  = _parentFunction;

		_index = ++_sindex;
		
		loopBody = false; 
		loopHeader = false ;
		
		liveIn = new HashSet<String>();

		id = _index;
        tag = "Block " + id.toString();
	}


	public AbstractNode getEntry(){
		for(AbstractNode node:nodes ){
			if ( node instanceof VarGetNode || node instanceof ImmediateNode ){
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
	

	
	public ArrayList<BasicBlock> getSuccessors(){
		return successors;
	}
	
	
	public BasicBlock taken ;
	public BasicBlock fallThrough ;
	public BasicBlock parent;
	
	public void toDot(PrintWriter pw, boolean standalone, boolean printRegAllocation) {
		if (standalone) {
			pw.println("digraph {");
			pw.println("rankdir=\"TD\"");
		}

		pw.print("BB" + _index + " [shape=record label=\"{");
        pw.print( "Block " + id + " from " + bFrom + " to " + bTo + "\n");

		for (AbstractNode n: getNodes()) {
            if (n.isExecutable())
                if (printRegAllocation)
                    pw.print('|' + n.printAllocation());
                else
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
        pw.print( "Block " + id + "\n");
		pw.print("}\" ] " + "\n");
	}


	public boolean hasAssignmentTo(Variable var) {
		for (AbstractNode node: getNodes()) {
			if (node instanceof PhiNode )
				if (((PhiNode)node).variableName.equals(var.name))
					return true;

			if (node instanceof VarSetNode)
				if (((VarSetNode)node).memAddress.equals(var.name))
					return true;
		}
		return false;
	}


	public void addPhiNode(Variable var) {
		PhiNode phi = new PhiNode(var.name) ;
		if (this.lineIndex != null)
			phi.sourceIndex = this.lineIndex ;
		else {
		}
		nodes.add(0,phi);	
	}


	public AbstractNode lastAssignment(Variable var) {
		ListIterator<AbstractNode> li = nodes.listIterator(nodes.size());
		while( li.hasPrevious() ){
			AbstractNode node = li.previous();
			if (node instanceof PhiNode)
				if (((PhiNode)node).variableName.equals(var.name))
					return node;
            if (node instanceof VarSetNode)
                if (((VarSetNode)node).memAddress.equals(var.name))
                    return node;
		}
		return null;
	}


	public AbstractNode getPhiOperand(String variableName, int bblIndex) {
        for (AbstractNode node: getNodes()) {
            if (node instanceof PhiNode)
                if (((PhiNode)node).variableName.equals(variableName))
                    return ((PhiNode) node).rightOperands.get(bblIndex);
        }
        return null;
    }

	public void addPhiOperand(Variable leftOperands, AbstractNode lastAssignment, int bblIndex) {
		for (AbstractNode node: getNodes()) {
			if (node instanceof PhiNode) {
				if (((PhiNode)node).variableName.equals(leftOperands.name)) {
					((PhiNode)node).rightOperands.put(bblIndex , lastAssignment);
                }
			}
		}
	}
	
	public void rename(){
		// For each phi block
		for (AbstractNode node: getNodes()) {
			if (node instanceof PhiNode) {
				String name = ((PhiNode)node).originalVarName;
				String newName = Variable.generateNewName(name);
				((PhiNode)node).variableName = newName;
			}
		}

		for( AbstractNode node: getNodes()) {

		    // LHS
			if (node instanceof VarGetNode) {
				String src = ((VarGetNode)node).variableId;
				String address = Variable.getTopmostName(src);
				((VarGetNode)node).variableId = address;
			}

			// RHS
            if (node instanceof VarSetNode) {
                String name = ((VarSetNode)node).originalMemAddress;
                String newName = Variable.generateNewName(name);
                ((VarSetNode)node).memAddress = newName;
            }

			if (node instanceof IONode && ((IONode)node).writeData()) {
				AbstractNode src =((IONode)node).getOperandAtIndex(0);
				//String address = Variable.getTopmostName(src);
				//saji TODO new
				// ((IONode)node).operands.set(0, new LabelNode(address));
			}
		}
		
		for (BasicBlock b: getSuccessors()) {
			for (AbstractNode node: b.getNodes()) {
				if (node instanceof PhiNode) {
					String name = ((PhiNode)node).originalVarName;
					String newName = Variable.getTopmostName(name);
					((PhiNode)node).rightOperands.put(getIndex(), new LabelNode(newName));
				}
			}
		}

		for (BasicBlock b: immediateDominants) {
			if (b.getIndex() != this._index)
				b.rename();
		}

		for (AbstractNode node: getNodes()) {
			if (node instanceof PhiNode) {
				String name = ((PhiNode)node).originalVarName;
				Variable.popTopmostName(name);
			}
            if (node instanceof VarSetNode) {
                String name = ((VarSetNode)node).originalMemAddress;
                Variable.popTopmostName(name);
            }
		}
	}

	public int indexIR(int index) {
	    bFrom = index;
	    for (AbstractNode node: getNodes()) {
            // Make sure that PhiNodes have the same index as block start index
            if (node instanceof PhiNode) {
                node.sourceIndex = index;
            } else if (node.isExecutable()) {
                index += 2; // Makes room for spills
				node.sourceIndex = index;
			}
		}
		bTo = index;
        return bTo;
	}

    @Override
    public String toString() {
        return tag;
    }

	private Integer id;
	private static int counter = 0 ;
	public static String generateID (String str) {
		return str + counter++;
	}

	public static  int _sindex = 0;
	public static  int _lindex = 0;
	private Integer _index;
	private List<AbstractNode> nodes;
	public ArrayList<BasicBlock> successors;
	public ArrayList<BasicBlock> predecessors;

	public HashSet<BasicBlock> dominators; // Blocks that are dominating this
	public HashSet<BasicBlock> dominatorsTemp;
	public HashSet<BasicBlock> immediateDominants;
	//public HashSet<BasicBlock> dominants; // Blocks this one is dominating
	public HashSet<BasicBlock> dominatorFrontiers;
	public Function parentFunction;


	public boolean loopHeader ;
	public boolean loopBody ;
	public Set<String> liveIn ;

	public int bFrom ;
	public int bTo ;
    public String tag;


    private Integer lineIndex; // a lineindex used for phi nodes

    public Integer getID() {
        return id;
    }
}
