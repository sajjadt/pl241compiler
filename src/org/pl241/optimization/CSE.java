package org.pl241.optimization;

import java.util.HashMap;
import java.util.Iterator;

import org.pl241.ir.AbstractNode;
import org.pl241.ir.BasicBlock;
import org.pl241.ir.LoadNode;
import org.pl241.ir.Function;
import org.pl241.ir.MoveNode;
import org.pl241.ir.PhiNode;

public class CSE {
	private HashMap<Expression,String> expressionMap;
	private HashMap<String,String> matchMap ;
	public CSE(){
		expressionMap = new HashMap<Expression,String>();
		matchMap = new HashMap<String, String>();
	}
	public void eliminate(Function function){
		fixBlock( function.getEntry() );
		
	}
	
	
	
	
	
	public void fixBlock(BasicBlock block){
		// Foreach phi function
		
		
		// Foreach Statement
		for( AbstractNode node: block.getNodes() ){
			if( node.operator.equals("ADD") || node.operator.equals("MUL") ){ ///TODO add more div, ...
				// look for operands
			
				String node1Label = node.getOperand1() ;
				String node2Label = node.getOperand2() ;
								
				node.setOperands(node1Label,node2Label);
				//node.operand1Label = node1Label ;
				//node.operand2Label = node2Label ;
				
				AbstractNode node1 = block.parentFunction.irMap.get(node1Label);
				AbstractNode node2 = block.parentFunction.irMap.get(node2Label);
				
				String operand1 = null;
				String operand2 = null; 
				
				if( node1 instanceof LoadNode ){
					operand1 = ((LoadNode)node1).memAddress; // TODO with stride ?
				} else if ( node1.operator.equals("ADD") || node1.operator.equals("MUL") ){ // TODO 
					// find it in the matchmap
					if( matchMap.containsKey(node1.label)){
						operand1 = matchMap.get(node1.label);
					}else {
						operand1 = node1.label ;
					}
				} else{
					
				}
				
				if( node2 instanceof LoadNode ){
					operand2 = ((LoadNode)node2).memAddress; //TODO 
				} else if ( node2.operator.equals("ADD") || node2.operator.equals("MUL") ){ //TODO
					if( matchMap.containsKey(node2.label)){
						operand2 = matchMap.get(node2.label);
					} else {
						operand2 = node2.label ;
					}
				} else{
					
				}
				while( matchMap.containsKey(operand1) ){
					operand1 = matchMap.get(operand1);	
				}
				
				while( matchMap.containsKey(operand2) ){
					operand2 = matchMap.get(operand2);
				}
				if( operand1 != null && operand2 != null ){
					Expression exp = new Expression(operand1,operand2, node.operator);
					if( expressionMap.containsKey(exp) ){
						matchMap.put(node.label, expressionMap.get(exp) );
						System.out.println("1 mm putting " + node.label + ":" + expressionMap.get(exp) );
						//node.label = expressionMap.get(exp);
						node.removed = true ; 
						node.removeReason = "CSE" ;
						
						node1.removed = true ; 
						node1.removeReason = "CSE" ;
						
						node2.removed = true ; 
						node2.removeReason = "CSE" ;
						
					} else {
						node.setOperands(operand1, operand2);
						//node.operand1Label = operand1 ;
						//node.operand2Label = operand2 ;
						expressionMap.put(exp,node.label);
						System.out.println("em putting " + exp.operator +"," + exp.op1 +"," + exp.op2 + ":" +  node.label );
						//matchMap.put(node.label, node.label );
					}
				}
				
			}else if (node instanceof MoveNode  ){
				
				MoveNode mNode = (MoveNode)node;
				String nodeLable = mNode.getInputOperands().get(0);
				//System.out.println("2 mm checking " + 	nodeLable);
				while( matchMap.containsKey(nodeLable) ){
					//System.out.println("found " + 	nodeLable + " " + matchMap.get(nodeLable));
					nodeLable = matchMap.get(nodeLable);	
					//System.out.println("2 mm checking " + 	nodeLable);
				}
				mNode.setRightOperand(nodeLable) ;
				matchMap.put( mNode.memAddress, nodeLable  );
				System.out.println("2 mm putting " + 	mNode.memAddress + ":" +  nodeLable );
			}
		}
		
		
		// Foreach successor
		for( BasicBlock nextBlock: block.getSuccessors() )
		{
			// handle phi functions
		}
		
		
		// Foreach child in DFT
		for( BasicBlock iBlock: block.immediateDominants ){
			fixBlock(iBlock);
		}
		
		Iterator<AbstractNode> i = block.getNodes().iterator() ;
		while(i.hasNext()){
			AbstractNode node = i.next();
			if( node.removed )
				i.remove();
		}
	}
	
}
