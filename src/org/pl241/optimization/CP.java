package org.pl241.optimization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pl241.ir.AbstractNode;
import org.pl241.ir.BasicBlock;
import org.pl241.ir.LoadNode;
import org.pl241.ir.Function;
import org.pl241.ir.MoveNode;
import org.pl241.ir.PhiNode;

public class CP {
	
	
	public void fixFunction(Function function){
		Map<String,String> copyTable = new HashMap<String,String>();
		
		for(BasicBlock block: function.blocks )
		{
			for(AbstractNode node: block.getNodes() ){
				// Left Side		
				if( node instanceof MoveNode ){
					// Find source operand
					
					//TODO array
					
					String key = ((MoveNode)node).getInputOperands().get(0) ;
					if ( function.irMap.containsKey( key ) ){
						AbstractNode tnode = function.irMap.get(key);
						if( tnode instanceof LoadNode ){
							String src = ((MoveNode)node).memAddress;
							String dst = ((LoadNode)tnode).memAddress ;
							if( copyTable.containsKey(dst ))
								copyTable.put( src, copyTable.get(dst) 	);
							else
								copyTable.put( src, dst 	);
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
					//TODO function.irMap.remove(node.label);
					i.remove();
				}
			}
			
		}
		for(BasicBlock block: function.blocks )
		{
			for(AbstractNode node: block.getNodes() ){
				// First check the right side
				if(  node instanceof LoadNode ){ // rhs fetch
					String varName = ((LoadNode) node).memAddress ;
					if( copyTable.containsKey(varName) ){
						((LoadNode) node).memAddress = copyTable.get(varName);
					}
				}
				
				if(  node instanceof PhiNode ){ // rhs phi
					//for(int i = 0 ;i <   ( (PhiNode) node).rightOperands.size() ; ++i ){
					//	String label = ( (PhiNode) node).rightOperands.values().get(i) ;
					//	if ( copyTable.containsKey( label ) ){
					//		 ( (PhiNode) node).rightOperands.put(i , copyTable.get(label) );
					//	}
					//}
					
					for(int key :  ( (PhiNode) node).rightOperands.keySet() ){
						String label = ( (PhiNode) node).rightOperands.get(key) ;
						if ( copyTable.containsKey( label ) ){
							 ( (PhiNode) node).rightOperands.put(key , copyTable.get(label) );
						}
					}
				}
			}
		}
	}
}
