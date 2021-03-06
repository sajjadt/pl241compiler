package org.pl241.ir;

import java.io.PrintWriter;
import java.util.*;


public class Function  {

    public Function(){
        basicBlocks = new ArrayList<>();
        localVariables = new VarInfoTable();
        parameters = new HashMap<>();
        _index = _sindex++;
        this.program = null;
    }

    public void setProgram(Program program){
        this.program = program;
    }

	public BasicBlock getEntryBlock() {
		return basicBlocks.get(0);
	}

	public String getName(){
		return name ;
	}

    public void setBranchTargets() {
		for (BasicBlock b: this.getBasicBlocks()) {
			for (NodeContainer container: b.getNodes()) {
				if (container.node instanceof BranchNode) {
					// rewrite address
					if (!((BranchNode)container.node).isCall) {
                        if (b.getSuccessors().size() > 1) {
							((BranchNode)container.node).fallThroughBlock = b.fallThrough;
                            ((BranchNode)container.node).takenBlock = b.taken;
                        } else {
                            ((BranchNode)container.node).fallThroughBlock = b.fallThrough;
                        }
                    }
				}
			}
		}
	}

	public void toDot(PrintWriter pw, boolean standalone, boolean main, boolean printAllocation) {

		if (!standalone) {
			pw.println("subgraph " + "cluster" + _index + " {");
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
			b.toDot(pw, false, printAllocation);
			for (BasicBlock bs : b.getSuccessors()) {
				pw.print("BB" + b.getIndex() + " -> BB" + bs.getIndex() 
						+ " [tailport=s, headport=n");

				if (!labels.contains(bs))
					labels.add(bs);

				pw.println("]");
			}
        }
		pw.println("}");
		if (standalone) {
			pw.flush();
		}
	}
	
	public void domToDot(PrintWriter pw, boolean standalone, boolean main) {
		if (!standalone) {
			pw.println("subgraph " + "cluster" + _index + " {");
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
			for (BasicBlock bs : b.immediateDominants) {
				if ( bs.getIndex() ==  b.getIndex() )
					continue;
				pw.print("BB" + b.getIndex() + " -> BB" + bs.getIndex()
						+ " [tailport=s, headport=n");

				if (!labels.contains(bs))
					labels.add(bs);
				pw.println("]");
			}
		}
		pw.println("}");
		if (standalone) {
			pw.flush();
		}
	}

    public BasicBlock getBlockByID(Integer blockID) {
	    for (BasicBlock block: basicBlocks) {
	        if (block.getID().equals(blockID))
	            return block;
        }
        return null;
    }
	
	private void setPredecessors() {
		 for (BasicBlock i: getBasicBlocks())
			 for (BasicBlock j: i.getSuccessors())
                 j.addPredecessor(i);
	}
	
	private List<BasicBlock> getBasicBlocks() {
		return basicBlocks;
	}

	public void computeDominateDependance() {
		setPredecessors(); //For all basic basicBlocks within the function
		
		for (BasicBlock i : getBasicBlocks()) { // Starting set of dominators
			if (i == getEntryBlock()) {
				i.addDominator(i);
			}
			else {
				for (BasicBlock j: getBasicBlocks()) {
                    i.addDominator(j);
				}
			}
		}

        boolean change = true;
		while (change) {
			// Change this with dfs
			change = false;

			for (BasicBlock j: getBasicBlocks()) {
				for (BasicBlock i: j.getPredecessors()) {
                    j.getDominators().remove(j);
                    if (j.getDominators().retainAll(i.getDominators())) {
                        change = true;
                    }
                    j.getDominators().add(j);
                }
			}
		}
    }
	
	public void computeDominatorTree() throws AnalysisException {
		for( BasicBlock b: getBasicBlocks()) {
			b.dominatorsTemp.addAll(b.dominators);
		}
		BasicBlock entryBlock = getEntryBlock() ;

		HashSet<BasicBlock> setbl = new HashSet<>();
		Stack<BasicBlock> sbl = new Stack<>();

		sbl.push(entryBlock);
		setbl.add(entryBlock);

		while (!sbl.isEmpty()) {
			BasicBlock bl = sbl.pop();
			for (BasicBlock i: getBasicBlocks()) { // Starting set of dominators
				if (i.dominators.contains(bl))
					i.removeDominator(bl);
			}
			for (BasicBlock i : getBasicBlocks()) {
				if( i.dominators.size() == 1 && i.dominators.contains(i) && !setbl.contains(i)){
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

	public void computeDominatorFrontiers() {
		for (BasicBlock b : getBasicBlocks()) {

		    ArrayList<BasicBlock> dominants = new ArrayList<>();
            for (BasicBlock d: getBasicBlocks()) {
                if (d.getDominators().contains(b))
                    dominants.add(d);
            }

			for (BasicBlock d: dominants) {
               for (BasicBlock f: d.getSuccessors())
                   if (!dominants.contains(f) || b.getIndex() == f.getIndex())
                    b.dominatorFrontiers.add(f);
			}
		}
	}

	public void insertPhiFunctions() {

	    List<String> vars = new ArrayList<>();
	    vars.addAll(parameters.keySet());
	    vars.addAll(localVariables.getVariableNames());
	    // TODO global vars as well??

		for (String var: vars) {
			Set<BasicBlock> workList = new HashSet<>();
			Set<BasicBlock> everOnWorkList = new HashSet<>();
			Set<BasicBlock> alreadyHasPhiFunc = new HashSet<>();

			for (BasicBlock b: getBasicBlocks()) {
				if (b.hasAssignmentTo(var)) {
					workList.add(b);
				}
			}
			everOnWorkList.addAll(workList);

			while (workList.size() > 0) {
				Iterator<BasicBlock> iterator = workList.iterator();
				BasicBlock block = iterator.next();
				iterator.remove();

				for (BasicBlock df: block.dominatorFrontiers) {
					if (!alreadyHasPhiFunc.contains(df)) {
						df.addPhiNode(var);
						alreadyHasPhiFunc.add(df);
						if (!everOnWorkList.contains(df)) {
							workList.add(df);
							everOnWorkList.add(df);
						}
					}
				}
			}
		}
	}
	
	public void rename() {
		getEntryBlock().rename();
	}

	public void indexIR() {
		List<BasicBlock> blocks = getBlocksInLayoutOrder();
		int index = 0;
		for (BasicBlock b: blocks) {
			index = b.indexIR(index);
			index += 2;
		}
	}

	public List<BasicBlock> getBlocksInLayoutOrder() {
		List<BasicBlock> blocks = new ArrayList<>();
		Stack<BasicBlock> stack = new Stack<>();
		// Pre-order traversal of Dominator tree
		stack.push(getEntryBlock());

		while (!stack.isEmpty()) {
			BasicBlock block = stack.pop();
			blocks.add(block);
			
			// Then others
			for (BasicBlock b: block.immediateDominants) {
				if (b != block.fallThrough && b != block.taken)
					stack.push(b);
			}
			// Then if
			for (BasicBlock b: block.immediateDominants) {
				if (b == block.taken) {
					stack.push(b);
					break;
				}
			}
			// First fallthrough 
			for (BasicBlock b: block.immediateDominants) {
				if (b == block.fallThrough) {
					stack.push(b);
					break;
				}
			}
		}
		return blocks;
	}

	// Insert branches to end of basic blocks
    public void insertBranches() {
		for (BasicBlock block: getBasicBlocks()) {


			if (block.successors.size() == 1 && block.fallThrough == null) {

                block.fallThrough = block.successors.get(0);
                if ( block.getNodes().size() == 0 || !block.getLastNode().isControlFlow()){
                    block.addNode(new NodeContainer(new BranchNode()));
                }
			}

			// Return statement for procedures (they have no return)
            if (block.successors.size() == 0 && !(block.getLastNode().node instanceof ReturnNode)) {
                ReturnNode node = new ReturnNode(null);
                block.addNode(new NodeContainer(node));
            }
		}		
	}

    public void removeUnreachableFlowEdges() {
        for (BasicBlock block: getBasicBlocks()) {
            if (block.successors.size() == 1
                    && (block.getLastNode().node instanceof ReturnNode)) {
                block.taken = null;
                block.fallThrough = null;
                block.successors.clear();
            }
        }
    }

    void printVarInfo() {
        if (Objects.equals(name, "main"))
            System.out.println("Global vars");
        else
            System.out.println(name + " local vars");

        for (Variable var: localVariables.getVars())
            System.out.println(var.toString());

        if (parameters.size() > 0) {
            System.out.println(name + " parameters");
            for (String var : parameters.keySet())
                System.out.println(var);
        }
    }

    public void insertGlobalAccesses(VarInfoTable globalVars) {
        for (BasicBlock block: getBasicBlocks()) {
            block.insertGlobalAccesses(this.parameters, this.localVariables, globalVars);
        }
    }

    // Unique across program
    public String name;
    private static int _sindex = 0;
    private int _index;
    public VarInfoTable localVariables;
    public Map<String, Integer> parameters;
    public List<BasicBlock> basicBlocks;
    private Program program;
}
	
	

	

