package org.pl241.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractNode {
	public String name ;
	public String label;
	
	public String operator ;
	private static int counter = 0 ;

	public String getOperand1(){
		if( operands.size() > 0 ){
			return operands.get(0) ;	
		}
		else {
			return null;
		}
	}
	
	public String getOperand2(){
		return operands.get(1) ;	
	}
	
	public void setOperands(String operand1, String operand2){
		operands.clear();
		operands.add(operand1);
		operands.add(operand2);
	}
	protected List<String> operands ;
	
	
	protected int lineIndex ; // In HIR code
	
	public boolean removed ;
	public String removeReason ;
	
	public AbstractNode nextNode;
	 
	
	
	
	public AbstractNode(String _operator ){
		operator = _operator;
		label = "l" + counter++;
		removed = false ;
		lineIndex = 1 ;
		operands = new ArrayList<String>();
	}
	
	public int getLineIndex(){
		return lineIndex ;
	}
	
	
	public AbstractNode(String _operand1Label, String _operand2Label , String _operator){
		this(_operator);
		operands.add(_operand1Label) ;
		operands.add(_operand2Label);
	}
	
	public static enum NodeType {
		IMMEDIATE,
		
		NEG , 
		
		ADD ,
		SUB,
		MUL,
		DIV,
		CMP,
		
		ADDA,
		LOAD,
		STORE,
		MOVE, 
		END,
		BRA,
		BNE,
		PHI,
		BEQ,
		BLE,
		BLT,
		BGE,
		BGT,
		
		READ,
		WRITE,
		WLN
	};
	
	public static Map<String,String> operatorMap ;
	public static Map<NodeType,String> operatorMapR ;
	static {
		operatorMap = new HashMap<String, String >();
		operatorMap.put("+", "ADD" );
		operatorMap.put("-", "SUB" );
		operatorMap.put("*", "MUL" );
		operatorMap.put("/", "DIV" );
		operatorMap.put("=", "MOVE" );
		operatorMap.put("==", "BEQ" );
		operatorMap.put("!=", "BNE" );
		operatorMap.put("<", "BLT");
		operatorMap.put("<=", "BLE" );
		operatorMap.put(">", "BGT" );
		operatorMap.put(">=", "BGE" );
		
		
		operatorMapR = new HashMap< AbstractNode.NodeType, String>();
		operatorMapR.put( NodeType.ADD , "ADD" );
		operatorMapR.put( NodeType.SUB ,"-" );
		operatorMapR.put( NodeType.MUL , "" );
		operatorMapR.put( NodeType.DIV ,"/");
		operatorMapR.put( NodeType.MOVE , "=");
		operatorMapR.put( NodeType.BEQ , "==");
		operatorMapR.put( NodeType.BNE , "!=");
		operatorMapR.put( NodeType.BLT , "<");
		operatorMapR.put( NodeType.BLE , "<=" );
		operatorMapR.put( NodeType.BGT , ">");
		operatorMapR.put( NodeType.BGE  , ">=");
		
		operatorMapR.put( NodeType.IMMEDIATE  , "Imm");
	}
	public BasicBlock parentBlock ;
	
	private  int _index ;
	public int getIndex() {
		return _index;
	}
	
	public String text ;
	
	public AbstractNode( ){
		text = ""; 
	}
	
	/*public String toString2() {		
		String ret = "lindex " + lineIndex +  " ";
		if( operator == "bra"){
			ret += label  + " bra " + "|";
		}
		else if( operator == "load"){
			ret += label +  " load " + "|";
		} else if ( operator == "imm" ){
			ret += label + " imm";
		}
		else if( operator == "=" ){
			
			ret += operand1.toString(); 
			ret +=  ( "|" +  operand2.toString() ); 
			ret +=  label + ( " move " + operand1.label + " " + operand2.label ) ;
		}	
		else {
			
			if( operand1 != null ) 
				ret += operand1.toString(); 
			
			if( operand2 != null ) 
				ret += "|" + operand2.toString();
			ret += "|" + label +" " +  operator + " " + (operand1!=null?operand1.label:"") +" " + (operand2!=null?operand2.label:"") ;
		}
		return ret;		
	}*/
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return  "lindex " + lineIndex +  " " + label +": " + operator + " " + operands.toString() ;
	}
	public List<AbstractNode> toList() {
		ArrayList<AbstractNode> list = new ArrayList<AbstractNode>() ;	
		return list;
	}
	
	
	public String getOutputOperand(){
		if( operator == "end" || operator == "imm"  )
			return null ;
		else
			return label;
	}

	public List<String> getInputOperands() {
		return operands;
	}

}
