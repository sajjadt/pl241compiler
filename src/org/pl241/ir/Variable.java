package org.pl241.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Variable {
	public String name;
	public enum VariableType{
		Integer, Array
	}
	
	
	public VariableType type;
	public ArrayList<Integer> dimensions;
	public static HashMap< String, Integer > counters ;
	public static HashMap< String, Stack<String> > recentIndices;
	
	static {
		counters = new HashMap<String, Integer>() ;
		recentIndices = new HashMap< String, Stack<String> > ();
	}
	public Variable(String _name, VariableType _type){
		name = _name;
		type = _type;	
	}
	
	public Variable(String _name, VariableType _type , ArrayList<Integer> _dimensions ){
		this(_name,_type);
		dimensions = _dimensions;
	}
	
	public String toString(){
		return name +" " + type.name() + " ";
	}
	
	public static String generateName(String originalName){
		if ( recentIndices.get(originalName) == null ){
			recentIndices.put(originalName , new Stack<String>() );
			counters.put(originalName, 0);
		}
		int index = counters.get(originalName) + 1 ;
		counters.put(originalName, index);
		String name =  originalName + "_"+index ;
		recentIndices.get(originalName).push(name);
		return name;
	}
	
	public static String getTopmostName(String originalName){
		if( recentIndices.get(originalName) != null )
			if( ! recentIndices.get(originalName).isEmpty() )
				return recentIndices.get(originalName).peek();
			else 
				return originalName ;
		else
			return originalName ;
	}
	
	public static String popTopmostName(String originalName){
		if (! recentIndices.get(originalName).empty() )
			return recentIndices.get(originalName).pop();
		return null;
	}
}
