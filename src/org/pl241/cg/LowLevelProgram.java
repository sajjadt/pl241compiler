package org.pl241.cg;

import org.pl241.Program;
import org.pl241.ir.AbstractNode;
import org.pl241.ir.BasicBlock;
import org.pl241.ir.Function;
import org.pl241.ir.Variable;
import org.pl241.ra.RegisterAllocator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.pl241.cg.DLXCodeGenerator.FRAMEP;
import static org.pl241.cg.DLXCodeGenerator.SP;

////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////
//                   Function Frame Map
// low address
//
//                   Local variables
//                      parameters
//
// high address
////////////////////////////////////////////////////////////


// Translates function codes into lower level instructions
public class LowLevelProgram {

    public LowLevelProgram() {
        lowLevelIR = new HashMap<>();
    }


    public void lowerAndAddFunction(Function f, RegisterAllocator allocator) {
        List<Instruction> instructions = new ArrayList<>();
        Map<Integer, Integer> blockMap = new HashMap<>();

        Integer currentIndex = 0;
        HashMap<String, Integer> localVarMap = new HashMap<>();


        // Allocate space for local vars (no need to initialize)
        // Also use this table keeps displacement of vars in regard of SP
        // Variables could be inside local function frame or global table
        // TODO: modify according to Stack allocation
        int displacement = 0;
        for (Variable var: f.localVariables.getVars()) {
            localVarMap.put(var.name, displacement);
            displacement += -4 * var.numElements();
        }


        List<BasicBlock> blocks = f.getBlocksInLayoutOrder();
        ListIterator li = blocks.listIterator(blocks.size());

        while(li.hasPrevious()) {
            BasicBlock block = (BasicBlock) li.previous();
            List<Instruction> blockInstructions = new ArrayList<>();

            List<AbstractNode> blockNodes = block.getNodes();
            ListIterator bi = blockNodes.listIterator(blockNodes.size());
            while (bi.hasPrevious()) {
                AbstractNode node = (AbstractNode) bi.previous();
                List<Instruction> ints = Instruction.lowerIRNode(node, allocator, currentIndex, blockMap, localVarMap, f.name.equals("main"));
                blockInstructions.addAll(0, ints);
                currentIndex += ints.size();
            }

            blockMap.put(block.getID(), currentIndex);
            instructions.addAll(0, blockInstructions);
        }

        currentIndex = 1;
        li = instructions.listIterator(instructions.size());
        // Fix missing branch targets
        while(li.hasPrevious()) {
            Instruction ins = (Instruction) li.previous();
            if (ins instanceof BranchInstruction &&
                    !((BranchInstruction) ins).resolved) {
                Integer offset = currentIndex - blockMap.get(((BranchInstruction) ins).destBlockID);
                ((BranchInstruction) ins).offset = offset;
                ((BranchInstruction) ins).resolved = true;
            }
            currentIndex += 1;
        }

        // Save SP in FrameP for user access to variables
        instructions.add(0, new Instruction(Instruction.Type.MOV,
                new Operand(Operand.Type.REGISTER, SP),
                null,
                new Operand(Operand.Type.REGISTER, FRAMEP)));
        // Make room for local variables
        instructions.add(1, new Instruction(Instruction.Type.ADDI,
                new Operand(Operand.Type.REGISTER, SP),
                new Operand(Operand.Type.IMMEDIATE, displacement),
                new Operand(Operand.Type.REGISTER, SP))
        );

        // Transfer parameters to their allocated space
        for (Variable var: f.localVariables.getVars()) {

        }

        System.out.println("Bmap:" + blockMap);
        lowLevelIR.put(f.name, instructions);
    }

    private HashMap<String, List<Instruction>> lowLevelIR;

    public List<Instruction> getFuncitonInstructions (Function func) {
        return lowLevelIR.get(func.name);
    }

    public void visualize(String path) throws IOException {
        File file = new File(path);
        file.createNewFile();

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("digraph {");
            for (String functionName: lowLevelIR.keySet()) {
                visualizeFunction(functionName, writer);
            }
            writer.println("}");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void visualizeFunction(String path, String functionName) throws IOException {
        File file = new File(path);
        file.createNewFile();

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("digraph {");
            visualizeFunction(functionName, writer);
            writer.println("}");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void visualizeFunction(String functionName, PrintWriter pw) {
        //pw.println("subgraph " + "cluster" + function. + " {");
        pw.println("label=" + functionName );
        pw.println("labelloc=\"t\";");
        pw.println("fontsize=18;");

        pw.println("rankdir=\"TD\"");

        pw.print("BB"  + " [shape=record label=\"{");
        pw.print( "Function " + functionName+ "\n");

        for (Instruction n: lowLevelIR.get(functionName)) {
            pw.print('|' + n.toString());
        }
        pw.print("}\" ] " + "\n");
//        pw.println("}");
    }

    public Program getIRProgram() {
        return irProgram;
    }
    public void setIRProgram(Program irProgram) {
        this.irProgram = irProgram;
    }
    private Program irProgram;
}
