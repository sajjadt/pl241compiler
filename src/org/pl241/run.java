package org.pl241;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.pl241.cg.DLX;
import org.pl241.cg.DLXCodeGenerator;
import org.pl241.frontend.*;
import org.pl241.frontend.Parser.ParseTreeNode;
import org.pl241.ir.BuildIRVisitor;
import org.pl241.ir.Function;
import org.pl241.ir.Program;
import org.pl241.ra.RegisterAllocator;

public class run
{

	public static void main(String[] args) throws IOException
	{
		int numRegs = 29 ;

		byte[] encoded = Files.readAllBytes(Paths.get("inputs/whwh.txt"));
		String input = new  String(encoded,  Charset.defaultCharset());

		Tokenizer tokenizer = new Tokenizer();
		Parser parser = new Parser();
		BuildIRVisitor visitor = new BuildIRVisitor() ;

		try {
			tokenizer.tokenize(input.trim());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			ParseTreeNode root = parser.parse(tokenizer.getTokens());
			root.accept(visitor); 
			Program program = visitor.getProgram();
			// to SSE form
			//program.process();
			program.visualize("a.dot");
			for (Function f:program.getFunctions()){
				// both should use the same layout ordering 
				f.indexIR();
				f.addMissingBranches();
			}
			
			program.visualize("b.dot");
			
			
			if ( false ) {
				program.copyPropagate();
				program.visualize("cp.dot");
				program.cse();
				
				for (Function f:program.getFunctions()){
					// both should use the same layout ordering 
					f.indexIR();
					f.addMissingBranches();
				}
				program.visualize("cse.dot");
				program.visualizeDominatorTree("b.dot");

				for( Function f:program.getFunctions()){

					RegisterAllocator allocator = new RegisterAllocator(numRegs, f);
					allocator.allocate();
					allocator.resolve();
				}

				DLXCodeGenerator generator = new DLXCodeGenerator(program);
				ArrayList<Integer> mem = generator.generateSampleCode();

				DLX.load(mem);
				DLX.execute();
				}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done with parsing");
				
		

		
		
		
	}

}