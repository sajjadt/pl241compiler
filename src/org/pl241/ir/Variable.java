package org.pl241.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Variable {

	public enum VariableType {
		INTEGER, ARRAY
	}

    Variable(String name, VariableType type) {
        this.name = name;
        this.type = type;
    }

    Variable(String name, VariableType type, ArrayList<Integer> dimensions) {
        this(name, type);
        this.dimensions = dimensions;
    }

	public int numElements() {
		if (type == VariableType.INTEGER) {
            return 1;
        } else {
		    int total = 1;
		    for (Integer i: dimensions) {
		        total *= i;
            }
            return total;
        }
	}

    public String toString() {
        String ret = name + " " + type.name() + " ";
        if (dimensions != null)
            ret += dimensions.toString();
        return ret;
    }

    static String generateNewName(String originalName) {
        if (recentIndices.get(originalName) == null) {
            recentIndices.put(originalName, new Stack<>());
            counters.put(originalName, 0);
        }

        int index = counters.get(originalName) + 1;
        counters.put(originalName, index);
        String newName =  originalName + "_" + index;
        recentIndices.get(originalName).push(newName);

        return newName;
    }

    static String getTopmostName(String originalName) {
        if (recentIndices.get(originalName) != null)
            if (!recentIndices.get(originalName).isEmpty())
                return recentIndices.get(originalName).peek();
            else
                return originalName;
        else
            return originalName;
    }

    static String popTopmostName(String originalName) {
        if (!recentIndices.get(originalName).empty())
            return recentIndices.get(originalName).pop();
        return null;
    }

    public ArrayList<Integer> getDimensions() {
	    return dimensions;
    }

    public VariableType type;
	private ArrayList<Integer> dimensions;

	private static HashMap<String, Integer> counters;
	private static HashMap<String, Stack<String>> recentIndices;
	static {
		counters = new HashMap<>();
		recentIndices = new HashMap<>();
	}

	public static void reset() {
	    counters.clear();
	    recentIndices.clear();
    }

    public String name;
}
