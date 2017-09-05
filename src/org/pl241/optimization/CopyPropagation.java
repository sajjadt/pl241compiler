package org.pl241.optimization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pl241.ir.*;
import org.pl241.ir.Function;

public class CopyPropagation {


    public void apply(Function function) {

        Map<String, String> copyTable = new HashMap<>();

        for (BasicBlock block : function.basicBlocks) {
            for (AbstractNode node : block.getNodes()) {
                // Left Side
                if (node instanceof VarSetNode) {
                    // Find source operand
                    AbstractNode tnode = node.getOperandAtIndex(0);

                    if (tnode instanceof VarGetNode) {
                        String src = ((VarSetNode) node).memAddress;
                        String dst = ((VarGetNode) tnode).variableId;

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
                    if (!(node instanceof VarGetNode))
                        i.remove();
                }
            }
        }

        // Replace
        for (BasicBlock block : function.basicBlocks) {
            for (AbstractNode node : block.getNodes()) {

                if (node instanceof PhiNode) { // rhs phi
                     for (int key: ((PhiNode)node).rightOperands.keySet()) {
                        AbstractNode label = ((PhiNode)node).rightOperands.get(key);
                        if (copyTable.containsKey(((LabelNode)label).getLabel())) {
                            ((PhiNode)node).rightOperands.put(key, new LabelNode(copyTable.get(((LabelNode) label).getLabel())));
                        }
                    }
                }

			    else if (node.getInputOperands().size() > 0) {
                    int index = 0;
                    for (AbstractNode inputNode: node.getInputOperands()) {
                        if (inputNode instanceof VarGetNode) {
                            if (copyTable.containsKey(((VarGetNode) inputNode).variableId)) {
                                System.out.println("CP: Replace " + inputNode + " with" + copyTable.get(((VarGetNode) inputNode).variableId));
                                ((VarGetNode) inputNode).variableId = copyTable.get(((VarGetNode) inputNode).variableId);
                            }
                        }
                        ++index;
                    }
                }

            }
        }
    }
}

