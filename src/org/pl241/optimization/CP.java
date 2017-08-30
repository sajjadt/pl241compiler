package org.pl241.optimization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pl241.ir.AbstractNode;
import org.pl241.ir.BasicBlock;
import org.pl241.ir.LoadNode;
import org.pl241.Function;
import org.pl241.ir.MoveNode;
import org.pl241.ir.PhiNode;

public class CP {
	
	
	public void apply(Function function){
		Map<AbstractNode,AbstractNode> copyTable = new HashMap<AbstractNode,AbstractNode>();
		
		for(BasicBlock block: function.basicBlocks)
		{
			for(AbstractNode node: block.getNodes() ){
				// Left Side		
				if( node instanceof MoveNode ){
					// Find source operand
					
					//TODO array
					
					AbstractNode key = ((MoveNode)node).getInputOperands().get(0) ;
					if ( function.irMap.containsKey( key ) ){
						AbstractNode tnode = function.irMap.get(key);
						if( tnode instanceof LoadNode ){
							String src = ((MoveNode)node).memAddress;
							String dst = ((LoadNode)tnode).memAddress ;
							// TODO saji
							//if( copyTable.containsKey(dst ))
							//	copyTable.put(src, copyTable.get(dst) 	);
							//else
							//	copyTable.put(src, dst);
							node.removed = true;
							tnode.removed = true;
							break;
						}
						
					}
					
					// if its a fetch
					// Remove both of them
					// Add to dictionary
				}
				
			}
			Iterator<AbstractNode> i = block.getNodes().iterator();
			while (i.hasNext()) {
				AbstractNode node = i.next();
				if (node.removed ){
					//TODO function.irMap.remove(node.uniqueLabel);
					i.remove();
				}
			}
			
		}
		for(BasicBlock block: function.basicBlocks)
		{
			for(AbstractNode node: block.getNodes() ){
				// First check the right side
				if(  node instanceof LoadNode ){ // rhs fetch
					String varName = ((LoadNode) node).memAddress ;
					if( copyTable.containsKey(varName) ){
					    // TODO saji
						// ((LoadNode) node).memAddress = copyTable.get(varName);
					}
				}
				
				if(  node instanceof PhiNode ){ // rhs phi
					//for(int i = 0 ;i <   ( (PhiNode) node).rightOperands.size() ; ++i ){
					//	String uniqueLabel = ( (PhiNode) node).rightOperands.values().get(i) ;
					//	if ( copyTable.containsKey( uniqueLabel ) ){
					//		 ( (PhiNode) node).rightOperands.put(i , copyTable.get(uniqueLabel) );
					//	}
					//}
					
					for(int key :  ( (PhiNode) node).rightOperands.keySet() ){
						AbstractNode label = ( (PhiNode) node).rightOperands.get(key) ;
						if ( copyTable.containsKey( label ) ){
							 ( (PhiNode) node).rightOperands.put(key , copyTable.get(label) );
						}
					}
				}
			}
		}
	}
}
