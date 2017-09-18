package org.pl241.ir;

import javax.xml.soap.Node;
import java.io.PrintWriter;
import java.util.*;

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


	public NodeContainer getEntry(){
		for(NodeContainer container:nodes) {
			if ( container.node instanceof VarGetNode || container.node instanceof ImmediateNode ){
				continue;
			}
			return container;
		}
		return null ;
	}

	public NodeContainer getLastNode() {
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

	public void addPredecessor(BasicBlock pred) {
		assert (pred != null): "Adding null block";
        if (!predecessors.contains(pred))
		    predecessors.add(pred);
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
	
	public void addNode (NodeContainer node) {
	    //assert (!nodes.contains(node)) : "Duplicate node " + node.toString() + " added to " + this.toString() ;
        // TODO: modify IR-builder to prevent duplicate adds
        if(!nodes.contains(node))
            nodes.add(node);
	}
	
	public List<NodeContainer> getNodes() {
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

        if (isLoopHeader())
            pw.print("*Header*");

		for (NodeContainer n: getNodes()) {
            if (n.node.visualize())
                if (printRegAllocation)
                    pw.print('|' + n.node.printAllocation());
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
		for (NodeContainer node: getNodes()) {
			if (node.node instanceof PhiFunctionNode)
				if (((PhiFunctionNode)node.node).variableName.equals(variableName))
					return true;

			if (node.node instanceof VarSetNode)
				if (((VarSetNode)node.node).memAddress.equals(variableName))
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
		nodes.add(0, new NodeContainer(phi));
	}

	public NodeContainer getPhiOperand(String variableName, int bblIndex) {
        for (NodeContainer container: getNodes()) {
            if (container.node instanceof PhiFunctionNode)
                if (((PhiFunctionNode)container.node).variableName.equals(variableName))
                    return ((PhiFunctionNode) container.node).rightOperands.get(bblIndex);
        }
        return null;
    }

    public NodeContainer getPhiNode(String variableName) {
        for (NodeContainer container: getNodes()) {
            if (container.node instanceof PhiFunctionNode && ((PhiFunctionNode)container.node).variableName.equals(variableName))
                return container;
        }
        return null;
    }

	public void addPhiOperand(Variable leftOperands, NodeContainer lastAssignment, int bblIndex) {
		for (NodeContainer node: getNodes()) {
			if (node.node instanceof PhiFunctionNode) {
				if (((PhiFunctionNode)node.node).variableName.equals(leftOperands.name)) {
					((PhiFunctionNode)node.node).rightOperands.put(bblIndex , lastAssignment);
                }
			}
		}
	}
	
	public void rename(){
		// For each phi block
		for (NodeContainer node: getNodes()) {
			if (node.node instanceof PhiFunctionNode) {
				String name = ((PhiFunctionNode)node.node).originalVariableName;
				String newName = Variable.generateNewName(name);
				((PhiFunctionNode)node.node).variableName = newName;
			}
		}

		for (NodeContainer node: getNodes()) {
		    // RHS
			if (node.node instanceof VarGetNode) {
				String src = ((VarGetNode)node.node).variableId;
				String address = Variable.getTopmostName(src);
				((VarGetNode)node.node).variableId = address;
			}

			// LHS
            if (node.node instanceof VarSetNode) {
                String name = ((VarSetNode)node.node).originalMemAddress;
                String newName = Variable.generateNewName(name);
                ((VarSetNode)node.node).memAddress = newName;
            }
		}
		
		for (BasicBlock b: getSuccessors()) {
			for (NodeContainer node: b.getNodes()) {
				if (node.node instanceof PhiFunctionNode) {
					String name = ((PhiFunctionNode)node.node).originalVariableName;
					String newName = Variable.getTopmostName(name);
					((PhiFunctionNode)node.node).rightOperands.put(getIndex(), new NodeContainer(new LabelNode(newName)));
				}
			}
		}

		for (BasicBlock b: immediateDominants) {
			if (b.getIndex() != this._index)
				b.rename();
		}

		for (NodeContainer node: getNodes()) {
			if (node.node instanceof PhiFunctionNode) {
				String name = ((PhiFunctionNode)node.node).originalVariableName;
				Variable.popTopmostName(name);
			}
            if (node.node instanceof VarSetNode) {
                String name = ((VarSetNode)node.node).originalMemAddress;
                Variable.popTopmostName(name);
            }
		}
	}

	public int indexIR(int index) {
	    bFrom = index;
	    for (NodeContainer node: getNodes()) {
            // Make sure that PhiNodes have the same index as block start index
            if (node.node instanceof PhiFunctionNode) {
                node.node.sourceIndex = index;
            } else if (node.node.isExecutable()) {
                index += 2; // Makes room for spills
				node.node.sourceIndex = index;
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
	private List<NodeContainer> nodes;
	public ArrayList<BasicBlock> successors;
	private ArrayList<BasicBlock> predecessors;

	public HashSet<BasicBlock> dominators; // Blocks that are dominating this
	public HashSet<BasicBlock> dominatorsTemp;
	public HashSet<BasicBlock> immediateDominants;
	//public HashSet<BasicBlock> dominants; // Blocks this one is dominating
	public HashSet<BasicBlock> dominatorFrontiers;
	public Function parentFunction;


	public boolean isLoopHeader() {
	    return loopHeader;
    }
	public boolean loopHeader;


	private boolean loopBody ;
	public Set<String> liveIn ;

	public int bFrom ;
	public int bTo ;
    private String tag;


    private Integer lineIndex; // a lineindex used for phi nodes

    public BasicBlock taken;
    public BasicBlock fallThrough;


    public void insertGlobalAccesses(Map<String, Integer> parameters,  VarInfoTable localVariables, VarInfoTable globalVars) {

        for(int i = 0; i < nodes.size(); i++) {
            NodeContainer node = nodes.get(i);
            if (node.node instanceof VarGetNode ) {
                String name = ((VarGetNode)node.node).variableId;
                if (!parameters.containsKey(name) &&
                        !localVariables.getVars().contains(name)) {
                    if (globalVars.containsVar(name)) {
                        ((VarGetNode) node.node).accessGlobals = true;
                        //nodes.remove(i);
                        NodeContainer n = new NodeContainer(new VarGetNode(name));
                        node.node = new MemoryLoadNode(n);
                        nodes.add(i, n);
                        //i++;
                    }
                }
            }
            if (node.node instanceof VarSetNode ) {
                String name = ((VarSetNode)node.node).originalMemAddress;
                if (!parameters.containsKey(name) &&
                        !localVariables.getVars().contains(name)) {
                    if (globalVars.containsVar(name)) {
                        ((VarSetNode) node.node).accessGlobals = true;
                        nodes.remove(i);
                        NodeContainer n = new NodeContainer(new VarGetNode(name));
                        nodes.add(i, n);
                        NodeContainer n2 = new NodeContainer(new MemoryStoreNode(n, node.getOperandAtIndex(0)));
                        nodes.add(i+1, n2);
                        i++;
                    }
                }
            }
        }
    }
}
