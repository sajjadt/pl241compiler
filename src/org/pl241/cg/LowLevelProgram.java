package org.pl241.cg;

import org.pl241.Program;
import org.pl241.ir.AbstractNode;
import org.pl241.ir.BasicBlock;
import org.pl241.ir.BranchNode;
import org.pl241.ir.Function;
import org.pl241.ra.RegisterAllocator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class LowLevelProgram {

    public LowLevelProgram() {
        lowLevelIR = new HashMap<>();
        this.program = null;
    }

    public void fromIRProgram(Program program, RegisterAllocator allocator) {
        this.program = program;

        for(Function f: program.getFunctions()) {
            List<Instruction> instructions = new ArrayList<>();
            Map<Integer, Integer> blockMap = new HashMap<>();

            Integer currentIndex = 0;

            List<BasicBlock> blocks = f.getBlocksInLayoutOrder();
            ListIterator li = blocks.listIterator(blocks.size());

            while(li.hasPrevious()) {
                BasicBlock block = (BasicBlock) li.previous();
                List<Instruction> blockInstructions = new ArrayList<>();


                List<AbstractNode> blockNodes = block.getNodes();
                ListIterator bi = blockNodes.listIterator(blockNodes.size());
                while (bi.hasPrevious()) {
                    AbstractNode node = (AbstractNode) bi.previous();
                    List<Instruction> ints = Instruction.lowerIRNode(node, allocator, currentIndex, blockMap);
                    blockInstructions.addAll(0, ints);
                    currentIndex += ints.size();
                }

                blockMap.put(block.getID(), currentIndex);
                instructions.addAll(0, blockInstructions);
            }

            currentIndex = 0;
            // Fix missing branch targets
            for (Instruction ins: instructions) {
                if (ins instanceof BranchInstruction &&
                        !((BranchInstruction) ins).resolved) {
                    Integer offset = blockMap.get(((BranchInstruction) ins).destBlockID) - currentIndex;
                    ((BranchInstruction) ins).offset = offset;
                    ((BranchInstruction) ins).resolved = true;
                }
                currentIndex += 1;
            }

            lowLevelIR.put(f.name, instructions);
        }
    }

    private Program program;
    private HashMap<String, List<Instruction>> lowLevelIR;


    public Program getIRProgram() {
        return program;
    }


    public List<Instruction> getFuncitonInstructions (Function func) {
        return lowLevelIR.get(func.name);
    }

    public void visualize(String path) throws IOException {
        File file = new File(path);
        file.createNewFile();

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("digraph {");
            for (Function function : program.getFunctions()) {
                visualizeFunction(function, writer);
            }
            writer.println("}");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void visualizeFunction(Function function, PrintWriter pw) {
        //pw.println("subgraph " + "cluster" + function. + " {");
        pw.println("label=" + function.getName() );
        pw.println("labelloc=\"t\";");
        pw.println("fontsize=18;");

        pw.println("rankdir=\"TD\"");

        pw.print("BB"  + " [shape=record label=\"{");
        pw.print( "Function " + function.name + "\n");

        for (Instruction n: lowLevelIR.get(function.name)) {
            pw.print('|' + n.toString());
        }
        pw.print("}\" ] " + "\n");
//        pw.println("}");
    }

}
