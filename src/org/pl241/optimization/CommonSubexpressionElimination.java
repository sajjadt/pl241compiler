package org.pl241.optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.pl241.ir.Function;
import org.pl241.ir.*;

import javax.xml.soap.Node;

// CSE deals with Arithmetic operations
public class CommonSubexpressionElimination implements Optimization {

	public CommonSubexpressionElimination() {
		expressionMap = new HashMap<>();
		copyMap = new HashMap<>();
	}

	public void apply(Function function) {
        HashMap<String, NodeContainer> nodesMap = new HashMap<>();
        fixBlock(function.getEntryBlock(), nodesMap);
	}

	private void fixBlock(BasicBlock block, HashMap<String, NodeContainer> nodesMap){

	    System.out.println("CSE. processing block " + block.getID());

        HashSet<Expression> localExpressionSet = new HashSet<>();
        HashSet<String> localCopySet = new HashSet<>();

        for (NodeContainer node: block.getNodes()) {
            if (node.hasOutputVirtualRegister())
                nodesMap.put(node.getOutputVirtualReg(), node);
        }

		for (NodeContainer node: block.getNodes()) {
		    if(node.node instanceof ArithmeticNode ||
                    node.node instanceof PhiFunctionNode) {
				NodeContainer node1 = node.getOperandAtIndex(0) ;
                NodeContainer node2 = node.getOperandAtIndex(1) ;
				String operand1 = node1.getOutputVirtualReg();
				String operand2 = node2.getOutputVirtualReg();

				if (node1.hasOutputVirtualRegister() && copyMap.containsKey(operand1)) {
                    operand1 = copyMap.get(operand1);
                    node.setOperandAtIndex(0, nodesMap.get(operand1));
                    node1 = nodesMap.get(operand1);
                }
				if (node2.hasOutputVirtualRegister() && copyMap.containsKey(operand2)) {
                    operand2 = copyMap.get(operand2);
                    node.setOperandAtIndex(1, nodesMap.get(operand2));
                    node2 = nodesMap.get(operand2);
                }

                assert node1 != null: "CSE: Node associated with " + operand1 + " not found!";;
                assert node2 != null : "CSE: Node associated with " + operand2 + " not found!";

                Expression exp = null;
                if (node.node instanceof ArithmeticNode)
                    exp = new Expression(operand1, operand2, Expression.fromArithExpressions(((ArithmeticNode)node.node).operator));
                else if (node.node instanceof PhiFunctionNode)
                    exp = Expression.fromPhi(node);
                assert exp != null;

                if (expressionMap.containsKey(exp)) {
                    node.setRemoved(true);
                    copyMap.put(node.getId(), expressionMap.get(exp));
                    localCopySet.add(node.getId());
                } else {
                    expressionMap.put(exp, node.getOutputVirtualReg());
                    localExpressionSet.add(exp);
                }
			}
			// A new copy
			else if (node.node instanceof VarSetNode) {
                String operand = node.getOperandAtIndex(0).getOutputVirtualReg();
                if (copyMap.containsKey(operand)) {
                    operand = copyMap.get(operand);
                    node.setOperandAtIndex(0, nodesMap.get(operand));
                }
                if (node.getOperandAtIndex(0).hasOutputVirtualRegister()){
                    String dst = node.getOperandAtIndex(0).getOutputVirtualReg();
                    if (copyMap.containsKey(dst))
                        dst = copyMap.get(dst);
                    copyMap.put(node.getOutputVirtualReg(), dst);
                    localCopySet.add(node.getOutputVirtualReg());
                }
            } else if (node.isExecutable()) {
		        for (int i = 0; i < node.getInputOperands().size(); i++) {
		            NodeContainer operand = node.getOperandAtIndex(i);
		            if (operand.hasOutputVirtualRegister() &&
                            copyMap.containsKey(operand.getOutputVirtualReg())) {
		                node.setOperandAtIndex(i, nodesMap.get(copyMap.get(operand.getOutputVirtualReg())));
                    }
                }
            }
        }

		// Foreach successor
		for (BasicBlock nextBlock: block.getSuccessors()) {
			// handle phi functions
            for (NodeContainer node: nextBlock.getNodes()) {
                if (node.node instanceof PhiFunctionNode) {
                    // Check if phi node parameters are in the copy table
                    // Replace them with the copy

                    NodeContainer node1 = node.getOperandAtIndex(0) ;
                    NodeContainer node2 = node.getOperandAtIndex(1) ;
                    String operand1 = node1.getOutputVirtualReg();
                    String operand2 = node2.getOutputVirtualReg();

                    if (node1.hasOutputVirtualRegister() && copyMap.containsKey(operand1)) {
                        operand1 = copyMap.get(operand1);
                        node.setOperandAtIndex(0, nodesMap.get(operand1));
                    }
                    if (node2.hasOutputVirtualRegister() && copyMap.containsKey(operand2)) {
                        operand2 = copyMap.get(operand2);
                        node.setOperandAtIndex(1, nodesMap.get(operand2));
                    }
                }
            }
		}
		
		// Foreach child in DFT
		for (BasicBlock iBlock: block.immediateDominants) {
			fixBlock(iBlock, nodesMap);
		}
        copyMap.keySet().removeAll(localCopySet);
        expressionMap.keySet().removeAll(localExpressionSet);

		block.getNodes().removeIf(node -> node.isRemoved());

	}
	private HashMap<Expression, String> expressionMap;
	private HashMap<String, String> copyMap;

}
