package org.pl241.ir;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class BasicBlock {

	public BasicBlock(Function _parentFunction, String _tag){
		nodes = new ArrayList<>();
		successors = new ArrayList<>();
		predecessors = new ArrayList<>();
		dominators = new HashSet<>();
		dominatorsTemp = new HashSet<>();
		dominatorFrontiers = new HashSet<>();
		immediateDominants = new HashSet<>();
		parentFunction  = _parentFunction;

		_index = ++_sindex;
		
		loopBody = false; 
		loopHeader = false ;
		
		liveIn = new HashSet<>();

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

	public void addPredecessor(BasicBlock succ) {
		if (succ == null)
			throw new NullPointerException("Setting predecessor of basic block to null");
		predecessors.add(succ);
	}

	public void removePredecessor(BasicBlock succ) throws AnalysisException {
		if (succ == null)
			throw new NullPointerException("Trying to remove a null predecessor");
		if (!predecessors.contains(succ))
			throw new AnalysisException("The basic block is not a predecessor");
		predecessors.remove(succ);
	}

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

	
	public void toDot(PrintWriter pw, boolean standalone, boolean printRegAllocation) {
		if (standalone) {
			pw.println("digraph {");
			pw.println("rankdir=\"TD\"");
		}

		pw.print("BB" + _index + " [shape=record label=\"{");
        pw.print( "Block " + id + " from " + bFrom + " to " + bTo + "\n");

		for (AbstractNode n: getNodes()) {
            if (n.visualize())
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


	public boolean hasAssignmentTo(String variableName) {
		for (AbstractNode node: getNodes()) {
			if (node instanceof PhiFunctionNode)
				if (((PhiFunctionNode)node).variableName.equals(variableName))
					return true;

			if (node instanceof VarSetNode)
				if (((VarSetNode)node).memAddress.equals(variableName))
					return true;
		}
		return false;
	}


	public void addPhiNode(String variableName) {
		PhiFunctionNode phi = new PhiFunctionNode(variableName) ;
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
			if (node instanceof PhiFunctionNode)
				if (((PhiFunctionNode)node).variableName.equals(var.name))
					return node;
            if (node instanceof VarSetNode)
                if (((VarSetNode)node).memAddress.equals(var.name))
                    return node;
		}
		return null;
	}


	public AbstractNode getPhiOperand(String variableName, int bblIndex) {
        for (AbstractNode node: getNodes()) {
            if (node instanceof PhiFunctionNode)
                if (((PhiFunctionNode)node).variableName.equals(variableName))
                    return ((PhiFunctionNode) node).rightOperands.get(bblIndex);
        }
        return null;
    }

	public void addPhiOperand(Variable leftOperands, AbstractNode lastAssignment, int bblIndex) {
		for (AbstractNode node: getNodes()) {
			if (node instanceof PhiFunctionNode) {
				if (((PhiFunctionNode)node).variableName.equals(leftOperands.name)) {
					((PhiFunctionNode)node).rightOperands.put(bblIndex , lastAssignment);
                }
			}
		}
	}
	
	public void rename(){
		// For each phi block
		for (AbstractNode node: getNodes()) {
			if (node instanceof PhiFunctionNode) {
				String name = ((PhiFunctionNode)node).originalVariableName;
				String newName = Variable.generateNewName(name);
				((PhiFunctionNode)node).variableName = newName;
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
			if (node instanceof AtomicFunctionNode && ((AtomicFunctionNode)node).isAMemoryStore) {
				AbstractNode src = node.getOperandAtIndex(0);
				//String address = Variable.getTopmostName(src);
				//saji TODO new
				// ((IONode)node).operands.set(0, new LabelNode(address));
			}
		}
		
		for (BasicBlock b: getSuccessors()) {
			for (AbstractNode node: b.getNodes()) {
				if (node instanceof PhiFunctionNode) {
					String name = ((PhiFunctionNode)node).originalVariableName;
					String newName = Variable.getTopmostName(name);
					((PhiFunctionNode)node).rightOperands.put(getIndex(), new LabelNode(newName));
				}
			}
		}

		for (BasicBlock b: immediateDominants) {
			if (b.getIndex() != this._index)
				b.rename();
		}

		for (AbstractNode node: getNodes()) {
			if (node instanceof PhiFunctionNode) {
				String name = ((PhiFunctionNode)node).originalVariableName;
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
            if (node instanceof PhiFunctionNode) {
                node.sourceIndex = index;
            } else if (node.isExecutable()) {
                index += 2; // Makes room for spills
				node.sourceIndex = index;
			}
		}
		bTo = index;
        return bTo;
	}
    public Integer getID() {
        return id;
    }
    public static String generateID (String str) {
        return str + counter++;
    }
    @Override
    public String toString() {
        return tag;
    }




	private Integer id;
	private static int counter = 0 ;

	private static  int _sindex = 0;
	private static  int _lindex = 0;
	private Integer _index;
	private List<AbstractNode> nodes;
	public ArrayList<BasicBlock> successors;
	private ArrayList<BasicBlock> predecessors;

	public HashSet<BasicBlock> dominators; // Blocks that are dominating this
	public HashSet<BasicBlock> dominatorsTemp;
	public HashSet<BasicBlock> immediateDominants;
	//public HashSet<BasicBlock> dominants; // Blocks this one is dominating
	public HashSet<BasicBlock> dominatorFrontiers;
	private Function parentFunction;


	public boolean loopHeader ;
	private boolean loopBody ;
	public Set<String> liveIn ;

	public int bFrom ;
	public int bTo ;
    private String tag;


    private Integer lineIndex; // a lineindex used for phi nodes

    public BasicBlock taken;
    public BasicBlock fallThrough;


}
