package org.pl241.cg;

import org.pl241.ir.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
	public class AllocationInfo{
		
	}
	private Map<String,Variable> variables;
	public void VarInfoTable(){
		variables = new HashMap<String,Variable>();
	}
	
	public void add(Variable var){
		variables.put(var.name , var );
	}
	
	public Variable getVar(String id){
		return variables.get(id) ;
	}
	
	public List<Variable> getVars(){
		return new ArrayList<Variable>(variables.values());
	}
	
	@Override
	public String toString() {
		String ret = "" ;
		for( Variable s: variables.values() ){
			ret += s.toString() ;
		}
		return ret;
	}
}
