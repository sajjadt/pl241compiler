package org.pl241.optimization;

import java.util.HashMap;
import org.pl241.ir.Function;
import org.pl241.ir.*;

// CSE deals with Arithmetic operations
public class CommonSubexpressionElimination implements Optimization {

	public CommonSubexpressionElimination() {
		expressionMap = new HashMap<>();
		copyMap = new HashMap<>();
	}

	public void apply(Function function) {
		fixBlock(function.getEntryBlock());
	}

	private void fixBlock(BasicBlock block){
		// Foreach phi function
        for (AbstractNode node: block.getNodes()) {
            if (node instanceof PhiFunctionNode) { // Handle Phis
                Expression exp = Expression.fromPhi((PhiFunctionNode)node);
                String V = node.getOutputVirtualReg();

                if (expressionMap.containsKey(exp)) {

                } else {

                }
            }
        }

		// Foreach Statement
		for (AbstractNode node: block.getNodes()) {

		    if(node instanceof ArithmeticNode) {
				// look for operands
				AbstractNode node1 = node.getOperandAtIndex(0) ;
				AbstractNode node2 = node.getOperandAtIndex(1) ;

				String operand1 = node1.getOutputVirtualReg();
				String operand2 = node2.getOutputVirtualReg();

				while (copyMap.containsKey(operand1))
					operand1 = copyMap.get(operand1);

				while (copyMap.containsKey(operand2))
					operand2 = copyMap.get(operand2);

                Expression exp = new Expression(operand1, operand2,
                        Expression.fromArithExpressions(((ArithmeticNode)node).operator)
                );

                if (expressionMap.containsKey(exp)) {
                    copyMap.put(node.nodeId, expressionMap.get(exp));

                    node.removed = true;
                    node.removeReason = "CSE";

                    node1.removed = true;
                    node1.removeReason = "CSE";

                    node2.removed = true;
                    node2.removeReason = "CSE";

                } else {
                    expressionMap.put(exp, node.getOutputVirtualReg());
                }
			} else if (node instanceof VarSetNode) {
		        // Handle copies
            }

        }

		// Foreach successor
		for (BasicBlock nextBlock: block.getSuccessors()) {
			// handle phi functions
            for (AbstractNode node: nextBlock.getNodes()) {
                if (node instanceof PhiFunctionNode) { // Handle Phis

                }
            }
		}
		
		// Foreach child in DFT
		for (BasicBlock iBlock: block.immediateDominants) {
			fixBlock(iBlock);
		}

		block.getNodes().removeIf(node -> node.removed);

	}
	private HashMap<Expression, String> expressionMap;
	private HashMap<String, String> copyMap;

}
