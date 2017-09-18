package org.pl241.cg;

import org.pl241.ir.Program;
import org.pl241.ir.*;
import org.pl241.ra.Allocation;
import org.pl241.ra.RegisterAllocator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static org.pl241.cg.DLXCodeGenerator.FRAMEP;
import static org.pl241.cg.DLXCodeGenerator.GLOBALP;
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
        globalVarMap = new HashMap<>();
    }


    public void lowerAndAddFunction(Function f, VarInfoTable globals, RegisterAllocator allocator) {
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

            List<NodeContainer> blockNodes = block.getNodes();
            ListIterator bi = blockNodes.listIterator(blockNodes.size());
            while (bi.hasPrevious()) {
                NodeContainer node = (NodeContainer) bi.previous();
                List<Instruction> ints = Instruction.lowerIRNode(node.node, currentIndex, blockMap, localVarMap, f.name.equals("main"));
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


        // Transfer parameters to their allocated space
        // Add them to the beginning of the function
        for (String param: f.parameters.keySet()) {
            if(f.getEntryBlock().liveIn.contains(param)) {
                Allocation allocation = allocator.getAllocationAt(param, 0);
                assert allocation != null;
                assert allocation.type == Allocation.Type.GENERAL_REGISTER;

                instructions.add(0, new Instruction(Instruction.Type.LOADI,
                        new Operand(Operand.Type.REGISTER, FRAMEP),
                        new Operand(Operand.Type.IMMEDIATE, f.parameters.get(param)),
                        new Operand(Operand.Type.REGISTER, allocation.address)));

            }
        }

        // Transfer address of globals  to their allocated space
        // Add them to the beginning of the function
        for (Variable global: globals.getVars()) {
            if(f.getEntryBlock().liveIn.contains(global.name)) {
                if (global.type == Variable.VariableType.ARRAY)
                    continue;

                Allocation allocation = allocator.getAllocationAt(global.name, 0);
                assert allocation != null;
                assert allocation.type == Allocation.Type.GENERAL_REGISTER;

                assert globalVarMap.containsKey(global.name) : "global location " + global.name + " is not found in the memory";
                instructions.add(0, new Instruction(Instruction.Type.LOADI,
                        new Operand(Operand.Type.REGISTER, GLOBALP),
                        new Operand(Operand.Type.IMMEDIATE, globalVarMap.get(global.name)),
                        new Operand(Operand.Type.REGISTER, allocation.address)));

            }
        }

        for (Variable variable: f.localVariables.getVars()) {

            if(variable.type == Variable.VariableType.ARRAY &&
                    f.getEntryBlock().liveIn.contains(variable.name)) {
                Allocation allocation = allocator.getAllocationAt(variable.name, 0);
                assert allocation != null;
                assert allocation.type == Allocation.Type.GENERAL_REGISTER;

                instructions.add(0, new Instruction(Instruction.Type.ADDI,
                        new Operand(Operand.Type.REGISTER, FRAMEP),
                        new Operand(Operand.Type.IMMEDIATE, localVarMap.get(variable.name)),
                        new Operand(Operand.Type.REGISTER, allocation.address)));

            }
        }

        if (!f.name.equals("main")){
            // Save SP in FrameP to access parameters/local variables
            instructions.add(0, new Instruction(Instruction.Type.MOV,
                    new Operand(Operand.Type.REGISTER, SP),
                    null,
                    new Operand(Operand.Type.REGISTER, FRAMEP)));

            // Make room for local variables
            if (f.localVariables.getVars().size() > 0)
                instructions.add(1, new Instruction(Instruction.Type.ADDI,
                        new Operand(Operand.Type.REGISTER, SP),
                        new Operand(Operand.Type.IMMEDIATE, displacement),
                        new Operand(Operand.Type.REGISTER, SP)));
        }

        System.out.println("Bmap:" + blockMap);
        lowLevelIR.put(f.name, instructions);
    }


    public List<Instruction> getFuncitonInstructions (Function func) {
        return lowLevelIR.get(func.name);
    }

    public void visualize(String path) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        file.createNewFile();

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("digraph {");
            for (String functionName: lowLevelIR.keySet()) {
                String n = toDot(functionName);
                writer.print(n);
            }
            writer.println("}");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String toDot(String functionName) throws IOException {
        String ret ="" ;
        ret += "subgraph " +  functionName +"{";
        ret += visualizeFunction(functionName);
        ret += "}\n";
        return ret;
    }

    private String visualizeFunction(String functionName) {

        String ret = "";
        ret = ret + ("label=" + functionName ) + "\n";
        ret = ret +("labelloc=\"t\";") + "\n";
        ret = ret +("fontsize=18;")+ "\n";

        ret = ret +("rankdir=\"TD\"")+ "\n";

        ret = ret +("Fun" + functionName  + " [shape=record label=\"{");
        ret = ret +( "Function " + functionName+ "\n");

        for (Instruction n: lowLevelIR.get(functionName)) {
            ret = ret +('|' + n.toString());
        }
        ret = ret +("}\" ] " + "\n");

        return ret;
    }

    public Program getIRProgram() {
        return irProgram;
    }
    public void setIRProgram(Program irProgram) {
        this.irProgram = irProgram;
    }
    private Program irProgram;

    private HashMap<String, List<Instruction>> lowLevelIR;

    public void addGlobals(VarInfoTable vars) {
        // Set up global allocation table
        int globalIndex = 0;
        for (Variable var: vars.getVars()) {
            globalVarMap.put(var.name, globalIndex);
            globalIndex += var.numElements() * 4;
        }
    }
    private HashMap<String, Integer> globalVarMap;
}
