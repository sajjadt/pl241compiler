package org.pl241.ra;

import org.pl241.Function;
import org.pl241.ir.AbstractNode;
import org.pl241.ir.ArithmaticNode;
import org.pl241.ir.BasicBlock;
import org.pl241.ir.LoadNode;

public class RandomAllocator {
    public void allocate(Function function) {
        int regIndex = 0;
        for (BasicBlock block: function.getBlocksInLayoutOrder()) {
            for (AbstractNode node: block.getNodes()) {
                if (node instanceof ArithmaticNode ||
                        node instanceof LoadNode ) // TODO : what kind of nodes need to get allocated to regs
                {
                    function.allocationMap.put(node.uniqueLabel, new Allocation(Allocation.Type.REGISTER, regIndex));
                    regIndex += 1;
                }
            }
        }
    }
}
