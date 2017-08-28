package org.pl241.cg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.pl241.ir.AbstractNode;
import org.pl241.ir.Function;
import org.pl241.ir.Program;


public class DLXCodeGenerator {
	
	// Contains the list of DLX instructions
	private ArrayList<Integer> memLayout;
	// Register 29 is the stack pointer
	private final int SP = 29;
	private final int ZERO = 0 ;
	// Register 31 contains the return address when we use JSP
	private final int RP = 31;
	
	private final String[] arithSet = { "ADD" , "MUL" , "DIV" , "SUB"};
	private final String[] loadstoreSet = {"load" , "store" } ;
	private final String[] branchSet = { "BLT" , "bra" , "BNE"  , "BEQ" , "BLE" , "BLT" , "BGE" , "BGT"};
	private final String[] transferSet = {"move"} ;

	private final int memSize = 20000 ;
	private int stackAddress = 20000 ; // Grows downwards
	private int BSS = 10000 ; // Grows upwards
	
	private Program program;
	
	// Contains a map between the function IDs and their address in the memory
	private HashMap<Integer, Integer> functionAddresses;
		
	private final int TEMP_REGISTER = 10;
	
	public DLXCodeGenerator(Program program) {
		this.program = program;
		memLayout = new ArrayList<Integer>(memSize) ;
		functionAddresses = new HashMap<Integer, Integer>();
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
		// Initialize Registers
		
		// Allocate global vars
		
		
		// Generate functions
		for(Function f:program.getFunctions()){
			generateFuncBody(f);
		}
		// allocate local vars
		
		
		// Generate main
		
		return memLayout;
	}

	private void generateFuncBody(Function f) {
		// local vars
		// args
		// Ret Value
		
		for( AbstractNode ins : f.getExecutableStream()){
			if( Arrays.asList(arithSet).contains(ins.operator) ){
				
			} else if ( Arrays.asList(loadstoreSet).contains(ins.operator) ) {

			}
		}
		return;
	}

	private void generateCall(){
		// Set func address
		
	}
}
