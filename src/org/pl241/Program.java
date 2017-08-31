package org.pl241;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.pl241.ir.AnalysisException;
import org.pl241.ir.BasicBlock;
import org.pl241.optimization.CP;
import org.pl241.optimization.CSE;

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
        	CP cp = new CP();
        	cp.apply(f);
        }
        
	}
	public void cse(){
		for (Function f:functions){
        	CSE cse = new CSE();
        	cse.apply(f);
        }
	}

	public void toSSAForm() {
		for (Function f : functions) {
			f.computeDominateDependance();
			f.computeDominatorFrontiers();
			f.insertPhiFunction();
			try {
				f.computeDominatorTree();
			} catch (AnalysisException e) {
				e.printStackTrace();
			}
			f.rename();
		}
	}

	// Visualization
	public void visualize(String path) throws IOException {
		boolean first = true ;
		File file = new File(path);
		file.createNewFile();
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.println("digraph {");
			for (Function function : functions) {
	            String n = function.getName();
	            String name = n ; // function.getSourceLocation().getFileName().replace('/', '.').replace('\\', '.').replace(':', '.') + 
	            	//	"." + n + ".line" + function.getSourceLocation().getLineNumber();
	            function.toDot(writer, false, false );
	        }
			writer.println("}");
        	
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public void visualizeDominatorTree(String path){
		boolean first = true ;
		File file = new File(path);
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.println("digraph {");
			for (Function function : functions) {
	            String n = function.getName();
	            String name = n ; // function.getSourceLocation().getFileName().replace('/', '.').replace('\\', '.').replace(':', '.') + 
	            	//	"." + n + ".line" + function.getSourceLocation().getLineNumber();
	            function.domToDot(writer, false, false );
	        }
			writer.println("}");
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void indexIR(){
		for(Function foo:functions){
			foo.indexIR();
		}
	}
}
