package org.pl241.optimization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pl241.ir.*;
import org.pl241.ir.Function;

import javax.xml.soap.Node;

public class CopyPropagation implements Optimization {
    public void apply(Function function) {
        Map<String, String> copyTable = new HashMap<>();

        for (BasicBlock block: function.basicBlocks) {

            for (NodeContainer node: block.getNodes()) {
                // Left Side
                if (node.node instanceof VarSetNode) {
                    // Find source operand
                    AbstractNode tnode = node.node.getOperandAtIndex(0).node;

                    if (tnode.hasOutputVirtualRegister()) {
                        System.out.println("Has output reg");
                        String dst = ((VarSetNode) node.node).memAddress;
                        String src = tnode.getOutputVirtualReg();
                        System.out.println("Copy from " + src + " to " + dst);

                        if (copyTable.containsKey(src))
                            copyTable.put(dst, copyTable.get(src));
                        else
                            copyTable.put(dst, src);

                        node.setRemoved(true);
                    }

                    // Find copies
                    /*if (tnode instanceof VarGetNode) {
                        String src = ((VarSetNode) node).memAddress;
                        String dst = ((VarGetNode) tnode).variableId;
                        if (copyTable.containsKey(dst))
                            copyTable.put(src, copyTable.get(dst));
                        else
                            copyTable.put(src, dst);

                        node.removed = true;
                        tnode.removed = true;
                    }*/
                }
            }
            Iterator<NodeContainer> i = block.getNodes().iterator();
            while (i.hasNext()) {
                NodeContainer node = i.next();
                if (node.isRemoved()) {
                    // Load nodes have no effect
                    if (!(node.node instanceof VarGetNode))
                        i.remove();
                }
            }
        }

        // Replace
        for (BasicBlock block : function.basicBlocks) {
            for (NodeContainer container : block.getNodes()) {

                if (container.node instanceof PhiFunctionNode) { // rhs phi
                     for (int key: ((PhiFunctionNode)container.node).rightOperands.keySet()) {
                        AbstractNode label = ((PhiFunctionNode)container.node).rightOperands.get(key).node;
                        if (copyTable.containsKey(((LabelNode)label).getLabel())) {
                            ((PhiFunctionNode)container.node).rightOperands.put(key, new NodeContainer(new LabelNode(copyTable.get(((LabelNode) label).getLabel()))));
                        }
                    }
                }

			    else if (container.node.getInputOperands().size() > 0) {
                    for (NodeContainer input: container.node.getInputOperands()) {
                        if (input.node instanceof VarGetNode) {
                            if (copyTable.containsKey(((VarGetNode) input.node).variableId)) {
                                System.out.println("CP: Replace " + input.node + " with" + copyTable.get(((VarGetNode) input.node).variableId));
                                ((VarGetNode) input.node).variableId = copyTable.get(((VarGetNode) input.node).variableId);
                            }
                        }
                    }
                }

            }
        }
    }
}

