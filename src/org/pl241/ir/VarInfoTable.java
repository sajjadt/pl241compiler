package org.pl241.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VarInfoTable {

	VarInfoTable() {
		variables = new HashMap<>();
	}
	
	public void add(Variable var) {
		variables.put(var.name , var );
	}
	Variable getVar(String id){
		return variables.get(id) ;
	}

	public List<Variable> getVars(){
		return new ArrayList<>(variables.values());
	}

    public List<String> getVariableNames(){
	    return new ArrayList<>(variables.keySet());
    }

    @Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		for( Variable s: variables.values() ){
			ret.append(s.toString());
		}
		return ret.toString();
	}

	private Map<String, Variable> variables;

    public boolean containsVar(String name) {
        return variables.keySet().contains(name);
    }
}
