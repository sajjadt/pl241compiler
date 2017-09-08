package org.pl241.cg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.pl241.ir.*;
import org.pl241.ir.Function;
import org.pl241.Program;

// Generate Assembly instructions
// Handles initializer codes nad places fucntions in proper place
// Sets final call destinations for functions

public class DLXCodeGenerator {

    public DLXCodeGenerator(LowLevelProgram program) {
        this.lowLevelProgram = program;
        this.irProgram = lowLevelProgram.getIRProgram();
        this.memLayout = new ArrayList<>(Collections.nCopies(memSize/4, 0));
        this.functionMap = new HashMap<>();
        //this.globalVarMap = new HashMap<>();
    }

    public ArrayList<Integer> initialize () {
        ArrayList<Integer> initCode = new ArrayList<>();

        // Initialize globalMap
        // Global index keeps track of variables on HEAP
        // it increases
        int globalIndex = 0;
        Function main = irProgram.getMainFunction();

        for (Variable var: main.localVariables.getVars()) {
            globalVarMap.put(var.name, globalIndex);
            globalIndex += 1 * var.numElements();
        }

        // Other initializations go here!
        int currentIndex;
        // Set SP
        initCode.add(DLX.assemble(DLX.ADDI, this.SP, this.ZERO, stackAddress));

        // Reserve spot for jump to main
        int jumpToMainIndex = 1;

        initCode.add(DLX.assemble(DLX.JSR, functionMap.get("main")));
        return initCode;
    }

    public ArrayList<Integer> generateBinary() {
        ArrayList<Integer> functionCode = new ArrayList<>();
        int currentIndex = 0;

        // Figure out function mapping
        // There is one-to-one mapping from LL Instructions to machine instructions
        for (Function f: irProgram.getFunctions()) {
            functionMap.put(f.name, (CODE + currentIndex)*4);
            currentIndex += lowLevelProgram.getFuncitonInstructions(f).size();
        }

        // Set function call targets
        for (Function f: irProgram.getFunctions()) {
            List<Instruction> body = lowLevelProgram.getFuncitonInstructions(f);
            for (Instruction ins: body) {
                if (ins instanceof CallInstruction) {
                    ((CallInstruction) ins).jumpAddress = functionMap.get(((CallInstruction) ins).destFunc);
                }
            }
        }

        // Generate functions
        for (Function f: irProgram.getFunctions()) {
            List<Instruction> body = lowLevelProgram.getFuncitonInstructions(f);
            ArrayList<Integer> instructions = generateFuncBody(f, body);
            functionCode.addAll(instructions);
        }

        // Initialize and jump to main
        memLayout.addAll(0, initialize());
        // Add functions code
        memLayout.addAll(CODE, functionCode);

        System.out.println("Function map: " + functionMap);

        return memLayout;
    }

    private ArrayList<Integer> generateFuncBody(Function f, List<Instruction> lowLevelInstructions) {
        ArrayList<Integer> instructions = new ArrayList<>();

        System.out.println("Generating code for function: " + f.name);

        for (Instruction ins: lowLevelInstructions) {
            instructions.add(this.generateInstruction(ins));
        }

        return instructions;
    }


    private Integer generateInstruction (Instruction ins) {

        switch (ins.type) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case CMP:
            case NEG:
                return generateRegisterArithmetic(ins.type, ins.destinationOperand, ins.sourceOperand1, ins.sourceOperand2);
            case CMPI:
            case ADDI:
            case SUBI:
            case MULI:
            case DIVI:
                assert (ins.sourceOperand1.type == Operand.Type.REGISTER &&
                        ins.sourceOperand2.type == Operand.Type.IMMEDIATE);
                return generateImmArithmetic(ins.type, ins.destinationOperand, ins.sourceOperand1, ins.sourceOperand2.value);
            case BEQ:
            case BNE:
            case BLT:
            case BGE:
            case RET:
            case BLE:
            case BGT:
            case BRA:
                return generateBranch(ins.type, ins.sourceOperand1, new Operand(Operand.Type.IMMEDIATE, ((BranchInstruction)ins).offset));
            // TODO function call case JSR:
            case RDD:
                return generateIO(ins.type, ins.destinationOperand);
            case WRD:
                return generateIO(ins.type, ins.sourceOperand1);
            case WRL:
                return generateIO(ins.type, null);
                //TODO true for read instructions as well??
            case MOV:
                assert ins.sourceOperand2 == null;
                if (ins.sourceOperand1.type == Operand.Type.IMMEDIATE)
                    return DLX.assemble(DLX.ADDI, ins.destinationOperand.value, ZERO, ins.sourceOperand1.value);
                else
                    return DLX.assemble(DLX.ADD, ins.destinationOperand.value, ins.sourceOperand1.value, ZERO);
            case PSH:
                return DLX.assemble(DLX.PSH, ins.sourceOperand1.value, SP, -4);
            case POP:
                return DLX.assemble(DLX.POP, ins.sourceOperand1.value, SP, 4);
            case JSR:
                return DLX.assemble(DLX.JSR, ((CallInstruction)ins).jumpAddress);

            default:
                throw new IllegalArgumentException();
        }
    }

    private Integer generateImmArithmetic(Instruction.Type operation,
                                          Operand destAllocation,
                                          Operand src1Allocation,
                                          int immValue) {

        assert destAllocation.type == Operand.Type.REGISTER
                && src1Allocation.type == Operand.Type.REGISTER;

        switch (operation) {
            case ADDI:
                return DLX.assemble(DLX.ADDI, destAllocation.value, src1Allocation.value, immValue);
            case SUBI:
                return DLX.assemble(DLX.SUBI, destAllocation.value, src1Allocation.value, immValue);
            case MULI:
                return DLX.assemble(DLX.MULI, destAllocation.value, src1Allocation.value, immValue);
            case CMPI:
                return DLX.assemble(DLX.CMPI, destAllocation.value, src1Allocation.value, immValue);
            case DIVI:
                return DLX.assemble(DLX.DIVI, destAllocation.value, src1Allocation.value, immValue);
            default:
                throw new Error("Unsupported op " + operation);
        }
    }

    private Integer generateRegisterArithmetic(Instruction.Type operation,
                                               Operand destAllocation,
                                               Operand src1Allocation,
                                               Operand src2Allocation) {

        assert destAllocation.type == Operand.Type.REGISTER
                && src1Allocation.type == Operand.Type.REGISTER
                && src2Allocation.type == Operand.Type.REGISTER;

        switch (operation) {
            case ADD:
                return DLX.assemble(DLX.ADD, destAllocation.value, src1Allocation.value, src2Allocation.value);
            case SUB:
                return DLX.assemble(DLX.SUB, destAllocation.value, src1Allocation.value, src2Allocation.value);
            case MUL:
                return DLX.assemble(DLX.MUL, destAllocation.value, src1Allocation.value, src2Allocation.value);
            case CMP:
                return DLX.assemble(DLX.CMP, destAllocation.value, src1Allocation.value, src2Allocation.value);
            case DIV:
                return DLX.assemble(DLX.DIV, destAllocation.value, src1Allocation.value, src2Allocation.value);
            case NEG:
                return DLX.assemble(DLX.MULI, destAllocation.value, src1Allocation.value,-1);
            default:
                throw new Error("Unsupported op " + operation);
        }
    }

    private Integer generateBranch(Instruction.Type operation,
                                   Operand operand1,
                                   Operand operand2) {

        assert operand2.type == Operand.Type.IMMEDIATE;

        if (operation != Instruction.Type.BRA)
            assert operand1.type == Operand.Type.REGISTER;

        switch (operation) {
            case BEQ:
                return DLX.assemble(DLX.BEQ, operand1.value, operand2.value);
            case BGE:
                return DLX.assemble(DLX.BGE, operand1.value, operand2.value);
            case BGT:
                return DLX.assemble(DLX.BGT, operand1.value, operand2.value);
            case BLE:
                return DLX.assemble(DLX.BLE, operand1.value, operand2.value);
            case BNE:
                return DLX.assemble(DLX.BNE, operand1.value, operand2.value);
            case BRA:
                return DLX.assemble(DLX.BSR, operand2.value);
            case BLT:
                return DLX.assemble(DLX.BLT, operand1.value, operand2.value);
            case RET:
                return DLX.assemble(DLX.RET, operand1.value);
            default:
                throw new Error("Unsupported op " + operation);
        }
    }

    private Integer generateIO(Instruction.Type operation, Operand operand) {

        ArrayList<Integer> instructions = new ArrayList<>();
        switch (operation) {
            case WRD:
                return DLX.assemble(DLX.WRD, operand.value);
            case RDD:
                return DLX.assemble(DLX.RDI, operand.value);
            case WRL:
                return DLX.assemble(DLX.WRL);
            default:
                throw new Error("Unsupported op " + operation);
        }

    }

/*


    private ArrayList<Integer> generateBlock(Function f, BasicBlock b, HashMap<String, Integer> localVarMap) {

        System.out.println("Generating block: " + b);
        System.out.println();


        ArrayList<Integer> instructions = new ArrayList<>();
        Allocation allocation = null;
        // Handle instructions
        for (AbstractNode ins : b.getNodes()) {
            if (ins instanceof VarGetNode) {
                System.out.println("Generating load: " + ins.toString());

                // Destination of load instruction
                allocation = ins.allocation;
                assert allocation.type == Allocation.Type.REGISTER;
                // TODO use temp register for memory allocated vars

                if (localVarMap.containsKey(((VarGetNode) ins).variableId)) {
                    instructions.add(DLX.assemble(DLX.LDW, allocation.address, this.FRAMEP, localVarMap.get(((VarGetNode) ins).variableId)));
                } else if (globalVarMap.containsKey(((VarGetNode) ins).variableId)) {
                    instructions.add(DLX.assemble(DLX.ADDI, this.TEMP_REGISTER, this.ZERO, BSS));
                    instructions.add(DLX.assemble(DLX.LDW, allocation.address, this.TEMP_REGISTER, globalVarMap.get(((VarGetNode) ins).variableId)));
                } else {
                    throw new Error("variable not found in both local and global map");
                }
            }
                instanceof VarSetNode) {
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

                if (localVarMap.containsKey(((VarSetNode) ins).originalMemAddress)) {
                    instructions.add(DLX.assemble(DLX.STW, allocation.address, this.FRAMEP, localVarMap.get(((VarSetNode) ins).originalMemAddress)));
                } else if (globalVarMap.containsKey(((VarSetNode) ins).originalMemAddress)) {
                    instructions.add(DLX.assemble(DLX.ADDI, this.TEMP_REGISTER, this.ZERO, BSS));
                    instructions.add(DLX.assemble(DLX.STW, allocation.address, this.TEMP_REGISTER, globalVarMap.get(((VarSetNode) ins).originalMemAddress)));
                } else {
                    throw new Error("variable not found in both local and global map");
                }

            }
        }
        return instructions;
    }





    */
	// Contains the list of DLX instructions
	private ArrayList<Integer> memLayout;

	public static final int ZERO = 0 ;
    public static final int TEMP_REGISTER = 27;
    public static final int RA = 31; // RA contains the return address when we use JSP
    public static final int FRAMEP = 28;
    public static final int SP = 29; // Register 29 is the stack pointer

    public static final int numRegisters = 32;

    // In bytes
    private final int memSize = 2000 ;
	private int stackAddress = 2000 ; // Grows downwards
	private int HEAP = 1000 ; // Grows upwards

    // In words
    private int CODE = 10 ; // Grows downwards. starts with Main.

    // Input program representations
	private LowLevelProgram lowLevelProgram;
    private Program irProgram;

	// Contains a map between the function IDs and their address in the memory
	private HashMap<String, Integer> functionMap;
    private HashMap<String, Integer> globalVarMap = new HashMap<>();
}
