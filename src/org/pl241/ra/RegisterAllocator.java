package org.pl241.ra;

import java.util.List;

import org.pl241.ir.BasicBlock;
import org.pl241.Function;

public class RegisterAllocator { // Allocate Registers for one function
	private int numRegs ;
	private Function function;
	private LinearScanner scanner ;
	private BuildIntervals liveIntervalBuilder;
	private RandomAllocator randomAllocator;
	public RegisterAllocator(int _numRegs, Function _function ){ // SSA Form
		numRegs = _numRegs ;
		function = _function ;
		liveIntervalBuilder = new BuildIntervals();
		randomAllocator = new RandomAllocator();
	}
	public void allocate(boolean randomAllocation){
		if (randomAllocation) randomAllocator.allocate(function);
        else {
			// TODO block labeling introduces some holes
			liveIntervalBuilder.build(function.getBlocksInLayoutOrder());

			liveIntervalBuilder.printIntervals();
			for (BasicBlock b : function.basicBlocks) {
				System.out.println(b.getIndex() + " " + b.getLiveIn());
			}
			scanner = new LinearScanner(liveIntervalBuilder.getLiveIntervals(), numRegs);
			scanner.scan();
			List<LiveInterval> alloc = scanner.handled;
			System.out.println(alloc);
		}
	}
	public void resolve() {
		function.resolve(liveIntervalBuilder.getLiveIntervals());
	}

    public void printAllocation(Function function) {
        for (String var: function.allocationMap.keySet()){
            Allocation allocation = function.allocationMap.get(var);
            System.out.println(var + " mapped to " + allocation.toString());
        }
    }
}