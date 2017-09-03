package org.pl241.cg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import org.pl241.ir.*;
import org.pl241.ir.Function;
import org.pl241.Program;
import org.pl241.ra.Allocation;
public class DLXCodeGenerator {

    public DLXCodeGenerator(Program program) {
        this.program = program;
        this.memLayout = new ArrayList<>(Collections.nCopies(memSize/4, 0));
        this.functionMap = new HashMap<>();
        this.globalVarMap = new HashMap<>();
    }


    private ArrayList<Integer> generateImmArithmetic(ArithmeticNode.ArithmeticType operation,
                                                    Allocation destAllocation,
                                                    Allocation src1Allocation,
                                                    int immValue) {

        assert destAllocation.type == Allocation.Type.REGISTER
                && src1Allocation.type == Allocation.Type.REGISTER;

        ArrayList<Integer> instructions = new ArrayList<>();
        switch (operation) {
            case ADD:
                instructions.add(DLX.assemble(DLX.ADDI, destAllocation.address, src1Allocation.address, immValue));
                break;
            case SUB:
                instructions.add(DLX.assemble(DLX.SUBI, destAllocation.address, src1Allocation.address, immValue));
                break;
            case MUL:
                instructions.add(DLX.assemble(DLX.MULI, destAllocation.address, src1Allocation.address, immValue));
                break;
            case CMP:
                instructions.add(DLX.assemble(DLX.CMPI, destAllocation.address, src1Allocation.address, immValue));
                break;
            case DIV:
                instructions.add(DLX.assemble(DLX.DIVI, destAllocation.address, src1Allocation.address, immValue));
                break;
            //case NEG:
            //    instructions.add(DLX.assemble(DLX.MULI, destAllocation.address, src1Allocation.address,-1));
            //    break;
            default:
                throw new Error("Unsupported op " + operation);
                //break;
        }
        return instructions;
    }

    private ArrayList<Integer> generateRegisterArithmetic(ArithmeticNode.ArithmeticType operation,
                                      Allocation destAllocation,
                                      Allocation src1Allocation,
                                      Allocation src2Allocation) {

        assert destAllocation.type == Allocation.Type.REGISTER
                && src1Allocation.type == Allocation.Type.REGISTER
                && src2Allocation.type == Allocation.Type.REGISTER;

        ArrayList<Integer> instructions = new ArrayList<>();
        switch (operation) {
            case ADD:
                instructions.add(DLX.assemble(DLX.ADD, destAllocation.address, src1Allocation.address, src2Allocation.address));
                break;
            case SUB:
                instructions.add(DLX.assemble(DLX.SUB, destAllocation.address, src1Allocation.address, src2Allocation.address));
                break;
            case MUL:
                instructions.add(DLX.assemble(DLX.MUL, destAllocation.address, src1Allocation.address, src2Allocation.address));
                break;
            case CMP:
                instructions.add(DLX.assemble(DLX.ADD, destAllocation.address, src1Allocation.address, src2Allocation.address));
                break;
            case DIV:
                instructions.add(DLX.assemble(DLX.DIV, destAllocation.address, src1Allocation.address, src2Allocation.address));
                break;
            case NEG:
                instructions.add(DLX.assemble(DLX.MULI, destAllocation.address, src1Allocation.address,-1));
                break;
            default:
                throw new Error("Unsupported op " + operation);
        }
        return instructions;
    }

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

    private ArrayList<Integer> generateBlock(Function f, BasicBlock b, HashMap<String, Integer> localVarMap) {

        System.out.println("Generating block: " + b);
        System.out.println();


        ArrayList<Integer> instructions = new ArrayList<>();
        Allocation allocation = null;
        // Handle instructions
        for (AbstractNode ins : b.getNodes()) {
            if (ins instanceof LoadNode) {
                System.out.println("Generating load: " + ins.toString());

                // Destination of load instruction
                allocation = ins.allocation;
                assert allocation.type == Allocation.Type.REGISTER;
                // TODO use temp register for memory allocated vars

                if (localVarMap.containsKey(((LoadNode) ins).variableId)) {
                    instructions.add(DLX.assemble(DLX.LDW, allocation.address, this.FRAMEP, localVarMap.get(((LoadNode) ins).variableId)));
                } else if (globalVarMap.containsKey(((LoadNode) ins).variableId)) {
                    instructions.add(DLX.assemble(DLX.ADDI, this.TEMP_REGISTER, this.ZERO, BSS));
                    instructions.add(DLX.assemble(DLX.LDW, allocation.address, this.TEMP_REGISTER, globalVarMap.get(((LoadNode) ins).variableId)));
                } else {
                    throw new Error("variable not found in both local and global map");
                }
            } else if (ins instanceof ArithmeticNode) {
                System.out.println("Generating code for arith: " + ins.toString());
                AbstractNode first = ins.getOperandAtIndex(0);
                AbstractNode second = ins.getOperandAtIndex(1);


                Allocation destAllocation = ins.allocation;
                Allocation src1Allocation = first.allocation;
                Allocation src2Allocation = second.allocation;

                if (first instanceof ImmediateNode) {
                    if (second instanceof ImmediateNode) {
                        instructions.add(DLX.assemble(DLX.ADDI, TEMP_REGISTER, this.ZERO, ((ImmediateNode) first).getValue()));
                        Allocation tempAllocation = new Allocation(Allocation.Type.REGISTER, TEMP_REGISTER);
                        instructions.addAll(generateImmArithmetic(((ArithmeticNode) ins).operator, destAllocation, tempAllocation, ((ImmediateNode) second).getValue()));
                    } else {
                        instructions.addAll(generateImmArithmetic(((ArithmeticNode) ins).operator, destAllocation, src2Allocation, ((ImmediateNode) first).getValue()));
                    }
                } else if (second instanceof ImmediateNode) {
                    instructions.addAll(generateImmArithmetic(((ArithmeticNode) ins).operator, destAllocation, src1Allocation, ((ImmediateNode) second).getValue()));
                } else {
                    instructions.addAll(generateRegisterArithmetic(((ArithmeticNode) ins).operator, destAllocation, src1Allocation, src2Allocation));
                }
            } else if (ins instanceof IONode) {
                System.out.println("Generating code for io: " + ins.toString());
                switch (((IONode) ins).type) {
                    case WRITELINE:
                        instructions.add(DLX.assemble(DLX.WRL));
                        break;
                    case WRITE:
                        AbstractNode node = ins.getOperandAtIndex(0);
                        if (node instanceof ImmediateNode) {
                            instructions.add(DLX.assemble(DLX.ADDI, this.TEMP_REGISTER, this.ZERO, ((ImmediateNode) node).getValue()));
                            instructions.add(DLX.assemble(DLX.WRD, this.TEMP_REGISTER));
                        } else {
                            allocation = node.allocation;
                            // Write parameter must be inside register. Otherwise it has to be moved into temp register first.
                            assert allocation.type == Allocation.Type.REGISTER;
                            instructions.add(DLX.assemble(DLX.WRD, allocation.address));
                        }
                        break;
                    case READ:
                        allocation = ins.allocation;
                        // Read target must be a register. Otherwise temp register can be used..
                        assert allocation.type == Allocation.Type.REGISTER;
                        instructions.add(DLX.assemble(DLX.RDI, allocation.address));
                        break;
                }
            } else if (ins instanceof StoreNode) {
                System.out.println("Generating store: " + ins.toString());

                // Source of write instruction
                // x <- imm : move imm value into temp
                if (ins.getOperandAtIndex(0) instanceof ImmediateNode) {
                    instructions.add(DLX.assemble(DLX.ADDI, this.TEMP_REGISTER, this.ZERO, ((ImmediateNode) ins.getOperandAtIndex(0)).getValue()));
                    allocation = new Allocation(Allocation.Type.REGISTER, TEMP_REGISTER);
                } else {
                    allocation = ins.getOperandAtIndex(0).allocation;
                }
                assert allocation.type == Allocation.Type.REGISTER;
                // TODO use temp register for memory allocated vars

                if (localVarMap.containsKey(((StoreNode) ins).memAddress)) {
                    instructions.add(DLX.assemble(DLX.STW, allocation.address, this.FRAMEP, localVarMap.get(((StoreNode) ins).memAddress)));
                } else if (globalVarMap.containsKey(((StoreNode) ins).memAddress)) {
                    instructions.add(DLX.assemble(DLX.ADDI, this.TEMP_REGISTER, this.ZERO, BSS));
                    instructions.add(DLX.assemble(DLX.STW, allocation.address, this.TEMP_REGISTER, globalVarMap.get(((StoreNode) ins).memAddress)));
                } else {
                    throw new Error("variable not found in both local and global map");
                }

            } else if (ins instanceof ReturnNode) {
                if (Objects.equals(f.name, "main")) {
                    System.out.println("Program exit added.");
                    // Restore SP
                    instructions.add(DLX.assemble(DLX.ADD, this.SP, this.FRAMEP, 0));
                    // Return
                    instructions.add(DLX.assemble(DLX.RET, 0));
                } else {
                    System.out.println("Normal return added.");
                    // Restore SP
                    instructions.add(DLX.assemble(DLX.ADD, this.SP, this.FRAMEP, 0));
                    // Return
                    instructions.add(DLX.assemble(DLX.RET, this.RA));
                }
            } else if (ins instanceof BranchNode) {
                int offsest = 0;
                System.out.println("Generating for branch : " + ins.toString());
                switch (((BranchNode) ins).type) {
                    case BEQ:
                        break;
                    case BGE:
                        break;
                    case BLE:
                        break;
                    case BNE:
                        break;
                    case BRA:
                        break;
                    case BLT:
                        allocation = ins.getOperandAtIndex(0).allocation;
                        assert allocation.type == Allocation.Type.REGISTER;
                        System.out.println("Branch target is :" + ((BranchNode) ins).callTarget);
                        //instructions.add(DLX.assemble(DLX.BLT, allocation.address, 0));
                        break;
                }
            } else {
                System.out.println("Ignoring gen.code for abstract : " + ins.toString());
            }
        }
        return instructions;
    }

    private ArrayList<Integer> generateFuncBody(Function f) {
        HashMap<String, Integer> localVarMap = new HashMap<>();
        Integer temp;
        ArrayList<Integer> instructions = new ArrayList<>();
        int displacement = 0;
        System.out.println("Generating code for function: " + f.name);


        // Allocate space for local vars (no need to initialize)
        // Also use this table keeps displacement of vars in regard of SP
        // Variables could be inside local function frame or global table
        // TODO: modify according to Stack allocation
        for (Variable var: f.symbolTable.getVars()) {
            localVarMap.put(var.name, displacement);
            displacement += -1 * var.numElements();
        }

        // Save SP in FrameP for user access to variables
        temp = DLX.assemble(DLX.ADD, this.FRAMEP, this.SP, 0);
        instructions.add(temp);

        // Make room for local variables
        instructions.add(DLX.assemble(DLX.ADDI, this.SP, this.SP, displacement));

        for (BasicBlock block: f.getBlocksInLayoutOrder()) {
            instructions.addAll(generateBlock(f, block, localVarMap));
        }

        return instructions;
    }


    private ArrayList<Integer> generateCall(String dest) {
        ArrayList<Integer> instructions = new ArrayList<>();

        //TODO Save Registers
        //TODO Pass parameters

        // Call
        instructions.add(DLX.assemble(DLX.JSR, functionMap.get(dest)));
        //TODO Discard parameters
        //TODO Restore Registers

        return instructions;
    }

    public ArrayList<Integer> generateProgram() {

        // Initialize globalMap
        // Global index keeps track of variables on HEAP
        // it increases
        int globalIndex = 0;
        Function main = program.getMainFunction();
        for (Variable var: main.symbolTable.getVars()) {
            globalVarMap.put(var.name, globalIndex);
            globalIndex += 1 * var.numElements();
        }

        // Other initializations go here!
        int currentIndex;
        // Set SP
        memLayout.set(0, DLX.assemble(DLX.ADDI, this.SP, this.ZERO, stackAddress));

        // Reserve spot for jump to main
        int jumpToMainIndex = 1;
        currentIndex = jumpToMainIndex + 1;

        // Generate functions
        for (Function f: program.getFunctions()) {
            ArrayList<Integer> body = generateFuncBody(f);
            memLayout.subList(currentIndex, currentIndex + body.size()).clear();
            functionMap.put(f.name, currentIndex);
            memLayout.addAll(currentIndex, body);
            currentIndex += body.size();
        }

        // Jump to main
        memLayout.set(jumpToMainIndex, DLX.assemble(DLX.BEQ, 0, functionMap.get("main")));

        return memLayout;
    }
	// Contains the list of DLX instructions
	private ArrayList<Integer> memLayout;

	/////// Register Map ///////////////
	private final int ZERO = 0 ;

    private final int TEMP_REGISTER = 27;
    private final int FRAMEP = 28;
    private final int SP = 29; // Register 29 is the stack pointer
	// Register 31 contains the return address when we use JSP
	private final int RA = 31;
    ////////////////////////////////////
	
	private final String[] arithSet = { "ADD" , "MUL" , "DIV" , "SUB"};
	private final String[] loadstoreSet = {"load" , "store" } ;
	private final String[] branchSet = { "BLT" , "bra" , "BNE"  , "BEQ" , "BLE" , "BLT" , "BGE" , "BGT"};
	private final String[] transferSet = {"move"} ;

	// All in bytes
	private final int memSize = 2000 ;
	private int stackAddress = 2000 ; // Grows downwards
	private int BSS = 1000 ; // Grows upwards
    //

    // Input program
	private Program program;

	// Contains a map between the function IDs and their address in the memory
	private HashMap<String, Integer> functionMap;
    private HashMap<String, Integer> globalVarMap = new HashMap<>();
}
