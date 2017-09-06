package org.pl241.cg;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import org.pl241.ir.*;
import org.pl241.ir.Function;
import org.pl241.Program;
import org.pl241.ra.Allocation;
public class DLXCodeGenerator {

    public DLXCodeGenerator(LowLevelProgram program) {
        this.lowLevelProgram = program;
        this.irProgram = lowLevelProgram.getIRProgram();
        this.memLayout = new ArrayList<>(Collections.nCopies(memSize/4, 0));

        this.functionMap = new HashMap<>();
        //this.globalVarMap = new HashMap<>();
    }

    public ArrayList<Integer> generateBinary() {

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
        memLayout.set(0, DLX.assemble(DLX.ADDI, this.SP, this.ZERO, stackAddress));

        // Reserve spot for jump to main
        int jumpToMainIndex = 1;
        currentIndex = jumpToMainIndex + 1;

        // Generate functions
        for (Function f: irProgram.getFunctions()) {
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
        for (Variable var: f.localVariables.getVars()) {
            localVarMap.put(var.name, displacement);
            displacement += -1 * var.numElements();
        }

        // Save SP in FrameP for user access to variables
        temp = DLX.assemble(DLX.ADD, this.FRAMEP, this.SP, 0);
        instructions.add(temp);

        // Make room for local variables
        instructions.add(DLX.assemble(DLX.ADDI, this.SP, this.SP, displacement));

        for (Instruction ins: lowLevelProgram.getFuncitonInstructions(f)) {
            instructions.add(this.generateInstruction(ins, f.name == "main"));
        }

        return instructions;
    }


    private Integer generateInstruction (Instruction ins, boolean isMain) {

        switch (ins.type) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case CMP:
            case NEG:
                return generateRegisterArithmetic(ins.type, ins.destinationOperand, ins.sourceOperand2, ins.sourceOperand1);
            case ADDI:
            case SUBI:
            case MULI:
            case DIVI:
            case CMPI:
                if (ins.sourceOperand1.type == Operand.Type.IMMEDIATE)
                    return generateImmArithmetic(ins.type, ins.destinationOperand, ins.sourceOperand2, ins.sourceOperand1.value);
                else
                    return generateImmArithmetic(ins.type, ins.destinationOperand, ins.sourceOperand1, ins.sourceOperand2.value);
            case BEQ:
            case BNE:
            case BLT:
            case BGE:
            case RET:
            case BLE:
            case BGT:
            case BRA:
                return generateBranch(ins.type, ins.sourceOperand1, new Operand(Operand.Type.IMMEDIATE, ((BranchInstruction)ins).offset), isMain);
            // TODO function call case JSR:
            case RDD:
            case WRD:
            case WRL:
                return generateIO(ins.type, ins.sourceOperand1);
                //TODO true for read instructions as well??
            case MOV:
                if (ins.sourceOperand1.type == Operand.Type.IMMEDIATE)
                    return DLX.assemble(DLX.ADDI, ins.destinationOperand.value, ZERO, ins.sourceOperand1.value);
                else
                    return DLX.assemble(DLX.ADD, ins.destinationOperand.value, ZERO, ins.sourceOperand1.value);
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
                return DLX.assemble(DLX.ADD, destAllocation.value, src1Allocation.value, src2Allocation.value);
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
                                   Operand operand2,
                                   boolean fromMain) {

        assert operand2.type == Operand.Type.IMMEDIATE;

        if (operation != Instruction.Type.BRA)
            assert operand1.type == Operand.Type.REGISTER;

        switch (operation) {
            case BEQ:
                return DLX.assemble(DLX.BEQ, operand1.value, operand2.value);
            case BGE:
                return DLX.assemble(DLX.BGE, operand1.value, operand2.value);
            case BLE:
                return DLX.assemble(DLX.BEQ, operand1.value, operand2.value);
            case BNE:
                return DLX.assemble(DLX.BEQ, operand1.value, operand2.value);
            case BRA:
                return DLX.assemble(DLX.BSR, operand2.value);
            case BLT:
                return DLX.assemble(DLX.BLT, operand1.value, operand2.value);
            case RET:
                if (fromMain)
                    return DLX.assemble(DLX.RET, 0);
                else
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
            } else if (ins instanceof VarSetNode) {
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


    */
	// Contains the list of DLX instructions
	private ArrayList<Integer> memLayout;

	/////// Register Map ///////////////
	public static final int ZERO = 0 ;
    public static final int TEMP_REGISTER = 27;
    public static final int RA = 31;

    private final int FRAMEP = 28;
    private final int SP = 29; // Register 29 is the stack pointer
	// Register 31 contains the return address when we use JSP

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
	private LowLevelProgram lowLevelProgram;
    private Program irProgram;

	// Contains a map between the function IDs and their address in the memory
	private HashMap<String, Integer> functionMap;
    private HashMap<String, Integer> globalVarMap = new HashMap<>();
}
