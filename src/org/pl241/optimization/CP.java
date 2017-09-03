package org.pl241.optimization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pl241.ir.*;
import org.pl241.ir.Function;

public class CP {


    public void apply(Function function) {
        Map<String, String> copyTable = new HashMap<>();

        for (BasicBlock block : function.basicBlocks) {
            for (AbstractNode node : block.getNodes()) {
                // Left Side
                if (node instanceof StoreNode) {
                    // Find source operand
                    AbstractNode tnode = node.getOperandAtIndex(0);
                    if (tnode instanceof LoadNode) {

                        String src = ((StoreNode) node).memAddress;
                        String dst = ((LoadNode) tnode).variableId;

                        System.out.println("Copy detected from " + src + " to " + dst);

                        if (copyTable.containsKey(dst))
                            copyTable.put(src, copyTable.get(dst));
                        else
                            copyTable.put(src, dst);

                        node.removed = true;
                        tnode.removed = true;
                    }
                }
            }
            Iterator<AbstractNode> i = block.getNodes().iterator();
            while (i.hasNext()) {
                AbstractNode node = i.next();
                if (node.removed) {
                    // Load nodes have no effect
                    if (!(node instanceof LoadNode)) {
                        System.out.println("Removing node " + node);
                        i.remove();
                    }
                }
            }
        }

        // Replace
        for (BasicBlock block : function.basicBlocks) {
            for (AbstractNode node : block.getNodes()) {

                if (node instanceof PhiNode) { // rhs phi
                     for (int key: ((PhiNode)node).rightOperands.keySet()) {
                        AbstractNode label = ((PhiNode)node).rightOperands.get(key);
                        if (copyTable.containsKey(label)) {
                            //((PhiNode)node).rightOperands.put(key, copyTable.get(label));
                        }
                    }
                }

			    else if (node.getInputOperands().size() > 0) {
                    int index = 0;
                    for (AbstractNode inputNode: node.getInputOperands()) {
                        if (inputNode instanceof LoadNode) {
                            if (copyTable.containsKey(((LoadNode) inputNode).variableId)) {
                                System.out.println("Replace " + inputNode + " with" + copyTable.get(((LoadNode) inputNode).variableId));
                                ((LoadNode) inputNode).variableId = copyTable.get(((LoadNode) inputNode).variableId);
                            }
                        }
                        ++index;
                    }
                }

            }
        }
    }
}

