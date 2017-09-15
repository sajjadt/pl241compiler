package org.pl241.optimization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.pl241.ir.Function;
import org.pl241.ir.*;

// CSE deals with Arithmetic operations
public class CommonSubexpressionElimination implements Optimization {

	public CommonSubexpressionElimination() {
		expressionMap = new HashMap<>();
		copyMap = new HashMap<>();
	}

	public void apply(Function function) {
        HashMap<String, AbstractNode> nodesMap = new HashMap<>();
        fixBlock(function.getEntryBlock(), nodesMap);
	}

	private void fixBlock(BasicBlock block, HashMap<String, AbstractNode> nodesMap){

	    System.out.println("CSE. processing block " + block.getID());

        HashSet<Expression> localExpressionSet = new HashSet<>();
        HashSet<String> localCopySet = new HashSet<>();

        for (AbstractNode node: block.getNodes()) {
            if (node.hasOutputVirtualRegister())
                nodesMap.put(node.getOutputVirtualReg(), node);
        }

		for (AbstractNode node: block.getNodes()) {
		    if(node instanceof ArithmeticNode ||
                    node instanceof PhiFunctionNode) {
				AbstractNode node1 = node.getOperandAtIndex(0) ;
				AbstractNode node2 = node.getOperandAtIndex(1) ;
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
                if (node instanceof ArithmeticNode)
                    exp = new Expression(operand1, operand2, Expression.fromArithExpressions(((ArithmeticNode)node).operator));
                else if (node instanceof PhiFunctionNode)
                    exp = Expression.fromPhi((PhiFunctionNode)node);
                assert exp != null;

                if (expressionMap.containsKey(exp)) {
                    node.removed = true;
                    copyMap.put(node.nodeId, expressionMap.get(exp));
                    localCopySet.add(node.nodeId);
                } else {
                    expressionMap.put(exp, node.getOutputVirtualReg());
                    localExpressionSet.add(exp);
                }
			}
			// A new copy
			else if (node instanceof VarSetNode) {
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
		            AbstractNode operand = node.getOperandAtIndex(i);
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
            for (AbstractNode node: nextBlock.getNodes()) {
                if (node instanceof PhiFunctionNode) {
                    // Check if phi node parameters are in the copy table
                    // Replace them with the copy

                    AbstractNode node1 = node.getOperandAtIndex(0) ;
                    AbstractNode node2 = node.getOperandAtIndex(1) ;
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

		block.getNodes().removeIf(node -> node.removed);

	}
	private HashMap<Expression, String> expressionMap;
	private HashMap<String, String> copyMap;

}
