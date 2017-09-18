package org.pl241.ir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.pl241.ir.AnalysisException;
import org.pl241.ir.Function;
import org.pl241.optimization.CopyPropagation;
import org.pl241.optimization.CommonSubexpressionElimination;

public class Program {
	private List<Function> functions;

    public Program(){
        functions = new ArrayList<>();
    }
	public List<Function> getFunctions(){
		return functions;
	}
	public void addFunction(Function function){
		functions.add(function);
	}


	public Function getMainFunction() {
	    for (Function f: functions) {
	        if (Objects.equals(f.name, "main"))
	            return f;
        }
	    return null;
    }

    // Optimizations
	public void copyPropagate(){
		for (Function f:functions){
        	CopyPropagation cp = new CopyPropagation();
        	cp.apply(f);
        }
        
	}
	public void cse() {
		for (Function f: functions) {
        	CommonSubexpressionElimination cse = new CommonSubexpressionElimination();
        	cse.apply(f);
        }
	}

	public void toSSAForm() {

		for (Function f: functions) {
			try {
                f.computeDominateDependance();
                f.computeDominateDependance();
                f.computeDominatorTree();

                f.computeDominatorFrontiers();
                f.insertPhiFunctions();
            } catch (AnalysisException e) {
				e.printStackTrace();
			}

			f.rename();
		}
	}

	// Visualization
	public void visualize(String path, boolean printAllocation) throws IOException {
		boolean first = true ;
		File file = new File(path);
        file.getParentFile().mkdirs();
        file.createNewFile();
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.println("digraph {");
			for (Function function : functions) {
	            String n = function.getName();
	            String name = n ; // function.getSourceIndex().getFileName().replace('/', '.').replace('\\', '.').replace(':', '.') +
	            	//	"." + n + ".line" + function.getSourceIndex().getLineNumber();
	            function.toDot(writer, false, false, printAllocation);
	        }
			writer.println("}");
        	
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void visualizeDominatorTree(String path) throws IOException {
		boolean first = true ;
		File file = new File(path);
        file.getParentFile().mkdirs();
        file.createNewFile();
        try (PrintWriter writer = new PrintWriter(file)) {
			writer.println("digraph {");
			for (Function function : functions) {
	            String n = function.getName();
	            String name = n ; // function.getSourceIndex().getFileName().replace('/', '.').replace('\\', '.').replace(':', '.') +
	            	//	"." + n + ".line" + function.getSourceIndex().getLineNumber();
	            function.domToDot(writer, false, false );
	        }
			writer.println("}");
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void indexIR() {
		for(Function foo:functions) {
			foo.indexIR();
		}
	}

    public void printVarInfo() {
        for(Function foo:functions) {
            foo.printVarInfo();
        }
    }
}
