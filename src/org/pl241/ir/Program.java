package org.pl241.ir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.pl241.optimization.CP;
import org.pl241.optimization.CSE;
import org.pl241.ra.BuildIntervals;
import org.pl241.ra.LiveInterval;
import org.pl241.ra.RegisterAllocator;

public class Program {
	
	
	private List<Function> functions; 
	
	
	private VarInfoTable globalSymbolTable;
	
	
	public Program(){
		functions = new ArrayList<Function>();
		globalSymbolTable = new VarInfoTable();
		
	}
	
	
	public void copyPropagate(){
		for (Function f:functions){
        	CP cp = new CP();
        	cp.fixFunction(f);
        }
        
	}
	
	
	public void cse(){
		for (Function f:functions){
        	CSE cse = new CSE();
        	cse.eliminate(f);
        	f.setBranchTargets();
        }
	}
	
	public void process(){
		
        
        visualize("a.dot");
        for (Function f:functions){
        	f.computeDominateDependance();
        	f.computeDominatorFrontiers();
        	
        	System.out.println(f.name);
        	if ( f.name.equals("main") ){
        		System.out.println( globalSymbolTable.toString() ) ;
        	}
        	System.out.println( f.symbolTable.toString() ) ;
        	for( BasicBlock b: f.blocks ){
        		System.out.println("BBL: " +b.getIndex()+" ");
        		for( BasicBlock df: f.blocks ){
        			if( b.dominatorFrontiers.contains(df)){
        				System.out.print(df.getIndex()+" ");
        			}
        		}
        		System.out.println("");
        	}
        	f.insertPhiFunction();
        	try {
				f.computeDominatorTree();
			} catch (AnalysisException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	f.rename();
        }
        
        
        visualize("c.dot");
        
      
       
	}
	
	
	
	public void addFunction(Function function){
		functions.add(function);
	}
	

	
	
	
	public void indexIR(){
		for(Function foo:functions){
			foo.indexIR();
		}
	}
	

		
	public void visualize(String path){
		boolean first = true ;
		File file = new File(path);
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.println("digraph {");
			for (Function function : functions) {
	            String n = function.isMain() ? "Main" : function.getName();
	            String name = n ; // function.getSourceLocation().getFileName().replace('/', '.').replace('\\', '.').replace(':', '.') + 
	            	//	"." + n + ".line" + function.getSourceLocation().getLineNumber();
	            function.toDot(writer, false, false );
	        }
			writer.println("}");
        	
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void visualizeDominatorTree(String path){
		boolean first = true ;
		File file = new File(path);
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.println("digraph {");
			for (Function function : functions) {
	            String n = function.isMain() ? "Main" : function.getName();
	            String name = n ; // function.getSourceLocation().getFileName().replace('/', '.').replace('\\', '.').replace(':', '.') + 
	            	//	"." + n + ".line" + function.getSourceLocation().getLineNumber();
	            function.domToDot(writer, false, false );
	        }
			writer.println("}");
        	
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Function> getFunctions(){
		return functions;
	}
	

	
}
