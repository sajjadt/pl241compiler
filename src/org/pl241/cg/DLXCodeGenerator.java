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
        int MEMORY_SIZE = 20000;
        this.memLayout = new ArrayList<>(Collections.nCopies(MEMORY_SIZE /4, 0));
        this.functionMap = new HashMap<>();
        //this.globalVarMap = new HashMap<>();
    }

    private ArrayList<Integer> initialize() {
        ArrayList<Integer> initCode = new ArrayList<>();

        // Initialize globalMap
        // Global index keeps track of variables on GLOBALS_ADDRESS
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
        int STACK_ADDRESS = 20000;
        initCode.add(DLX.assemble(DLX.ADDI, this.SP, this.ZERO, STACK_ADDRESS));

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
        int CODE_ADDRESS = 10;
        for (Function f: irProgram.getFunctions()) {
            functionMap.put(f.name, (CODE_ADDRESS + currentIndex)*4);
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
        memLayout.addAll(CODE_ADDRESS, functionCode);

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
            case XOR:
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
            case RDD:
                return generateIO(ins.type, ins.destinationOperand);
            case WRD:
                return generateIO(ins.type, ins.sourceOperand1);
            case WRL:
                return generateIO(ins.type, null);
            case MOV:
                assert ins.sourceOperand2 == null;
                if (ins.sourceOperand1.type == Operand.Type.IMMEDIATE)
                    return DLX.assemble(DLX.ADDI, ins.destinationOperand.value, ZERO, ins.sourceOperand1.value);
                else
                    return DLX.assemble(DLX.ADD, ins.destinationOperand.value, ins.sourceOperand1.value, ZERO);
            case PSH:
                return DLX.assemble(DLX.PSH, ins.sourceOperand1.value, SP, 4);
            case POP:
                return DLX.assemble(DLX.POP, ins.destinationOperand.value, SP, 4);
            case JSR:
                return DLX.assemble(DLX.JSR, ((CallInstruction)ins).jumpAddress);
            case STORE:
                return DLX.assemble(DLX.STX, ins.sourceOperand1.value, ins.destinationOperand.value, ZERO);
            case LOAD:
                return DLX.assemble(DLX.LDX, ins.destinationOperand.value, ins.sourceOperand1.value, ins.sourceOperand2.value);
            case LOADI:
                return DLX.assemble(DLX.LDW, ins.destinationOperand.value, ins.sourceOperand1.value, ins.sourceOperand2.value);

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
            case XOR:
                return DLX.assemble(DLX.XOR, destAllocation.value, src1Allocation.value, src2Allocation.value);
            case ADD:
                return DLX.assemble(DLX.ADD, destAllocation.value, src1Allocation.value, src2Allocation.value);
            case SUB:
                return DLX.assemble(DLX.SUB, destAllocation.value, src1Allocation.value, src2Allocation.value);
            case MUL:
                return DLX.assemble(DLX.MUL, destAllocation.value, src1Allocation.value, src2Allocation.value);
            case CMP:
                Integer ins = DLX.assemble(DLX.CMP, destAllocation.value, src1Allocation.value, src2Allocation.value);
                System.out.println(DLX.disassemble(ins));
                return ins;
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
                return DLX.assemble(DLX.BEQ, ZERO, operand2.value);
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

	// Contains the list of DLX instructions
	private ArrayList<Integer> memLayout;

	// Constants
	public static final int ZERO = 0 ;
    public static final int SCRATCH_REGISTER = 27;
    public static final int RA = 31; // RA contains the return address when we use JSP
    public static final int FRAMEP = 28;
    public static final int SP = 29; // Register 29 is the stack pointer
    public static final int NUM_REGISTERS = 32;
    private final int GLOBALS_ADDRESS = 10000 ; // Grows upwards

    // Input program representations
	private LowLevelProgram lowLevelProgram;
    private Program irProgram;

	// Contains a map between the function IDs and their address in the memory
	private HashMap<String, Integer> functionMap;
    private HashMap<String, Integer> globalVarMap = new HashMap<>();
}
