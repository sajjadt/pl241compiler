package org.pl241.cg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import org.pl241.ir.*;
import org.pl241.Function;
import org.pl241.Program;


public class DLXCodeGenerator {
	
	// Contains the list of DLX instructions
	private ArrayList<Integer> memLayout;
	// Register 29 is the stack pointer
	private final int SP = 29;
	private final int FRAMEP = 28;
	private final int ZERO = 0 ;
	// Register 31 contains the return address when we use JSP
	private final int RP = 31;
	
	private final String[] arithSet = { "ADD" , "MUL" , "DIV" , "SUB"};
	private final String[] loadstoreSet = {"load" , "store" } ;
	private final String[] branchSet = { "BLT" , "bra" , "BNE"  , "BEQ" , "BLE" , "BLT" , "BGE" , "BGT"};
	private final String[] transferSet = {"move"} ;

	// All in bytes
	private final int memSize = 20000 ;
	private int stackAddress = 20000 ; // Grows downwards
	private int BSS = memSize ; // Grows upwards
    //
	
	private Program program;
	
	// Contains a map between the function IDs and their address in the memory
	private HashMap<String, Integer> functionAddresses;
		
	private final int TEMP_REGISTER = 10;
	
	public DLXCodeGenerator(Program program) {
		this.program = program;
        memLayout = new ArrayList<>(Collections.nCopies(memSize/4, 0));
		functionAddresses = new HashMap<>();
	}

	public ArrayList<Integer> generateSampleCode(){
		// Set the stack pointer in register R29, set it to 0 now and fix it later
		this.memLayout.add(DLX.assemble(DLX.ADDI, this.SP, this.ZERO, this.stackAddress));
		
		
		// Test Push/Pop
		this.memLayout.add(DLX.assemble(DLX.ADDI, this.TEMP_REGISTER, this.ZERO, 55));
		this.memLayout.add(DLX.assemble(DLX.PSH,  this.TEMP_REGISTER, this.SP, 4));
		
		this.memLayout.add(DLX.assemble(DLX.ADDI, this.TEMP_REGISTER, this.ZERO, 66));
		this.memLayout.add(DLX.assemble(DLX.PSH,  this.TEMP_REGISTER, this.SP,  4));
		
		this.memLayout.add(DLX.assemble(DLX.ADDI, this.TEMP_REGISTER, this.ZERO, 77));
		this.memLayout.add(DLX.assemble(DLX.PSH,  this.TEMP_REGISTER, this.SP,  4));
		
		// Jump to the beginning of the main function
		this.memLayout.add(DLX.assemble(DLX.POP, this.TEMP_REGISTER , this.SP, 4));
		this.memLayout.add(DLX.assemble(DLX.WRD, this.TEMP_REGISTER ));
		
		this.memLayout.add(DLX.assemble(DLX.POP, this.TEMP_REGISTER , this.SP, 4));
		this.memLayout.add(DLX.assemble(DLX.WRD, this.TEMP_REGISTER ));
		
		this.memLayout.add(DLX.assemble(DLX.POP, this.TEMP_REGISTER , this.SP, 4));
		this.memLayout.add(DLX.assemble(DLX.WRD, this.TEMP_REGISTER ));
		
		this.memLayout.add(DLX.assemble(DLX.RET, this.RP ));
		
		
		// Generate functions
		
		
		// Generate 
		
		return memLayout;
	}

	public ArrayList<Integer> generateCode(){


		// Jump to main
		int jumpToMainIndex = 0;
		// Generate functions
        int current_index = 1;
		for(Function f:program.getFunctions()){
			ArrayList<Integer> body = generateFuncBody(f);
            memLayout.subList(current_index, current_index + body.size()).clear();
            functionAddresses.put(f.name, current_index*4); // DLX is byte addressable
			memLayout.addAll(current_index, body);
            current_index += body.size();
		}

		// Jump to main
		memLayout.set(jumpToMainIndex, DLX.assemble(DLX.BEQ, 0, functionAddresses.get("main")));
		// Do we need any initialization?

		return memLayout;
	}

	private ArrayList<Integer> generateFuncBody(Function f) {
        ArrayList<Integer> instructions = new ArrayList<>();
		for( AbstractNode ins : f.getExecutableStream()){
			if (ins instanceof LoadNode) {
				if (Objects.equals(f.name, "main")) {

				} else {
				    // Load from Stack
                }
			} else if (ins instanceof ArithmaticNode) {

            } else if (ins instanceof IONode) {

            } else if (ins instanceof StoreNode) {
                if (Objects.equals(f.name, "main")) {

                } else {
                    // Load from Stack
                }
            } else if (ins instanceof ReturnNode) {

            } else if (ins instanceof BranchNode) {

            }
		}
		return instructions;
	}

	private void generateCall(){
		// Set func address
		
	}
}
