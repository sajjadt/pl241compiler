package org.pl241.ra;

import java.util.ArrayList;
import java.util.List;

import org.pl241.ir.BasicBlock;
import org.pl241.ir.Function;

public class RegisterAllocator { // Allocate Registers for one function
	private int numRegs ;
	private Function function;
	private LinearScanner scanner ;
	private BuildIntervals liveIntervalBuilder; 
	public RegisterAllocator(int _numRegs, Function _function ){ // SSA Form
		numRegs = _numRegs ;
		function = _function ;
		liveIntervalBuilder = new BuildIntervals();
		
	}
	public void allocate(){
		// TODO block labeling introduces some holes
		liveIntervalBuilder.build(function.getBlocksInLayoutOrder());
		
		liveIntervalBuilder.dumpIntervals();
		for(BasicBlock b:function.blocks ){
			System.out.println(b.getIndex() + " " + b.getLiveIn());
		}
		scanner = new LinearScanner(liveIntervalBuilder.getLiveIntervals() , numRegs) ;
		scanner.scan();
		List<LiveInterval> alloc = scanner.handled;
		System.out.println(alloc);
	}
	public void resolve() {
		function.resolve(liveIntervalBuilder.getLiveIntervals());
		
	}
	
}