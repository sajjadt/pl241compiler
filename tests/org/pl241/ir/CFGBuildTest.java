package org.pl241.ir;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.pl241.frontend.Parser;
import org.pl241.frontend.Parser.ParseTreeNode;
import org.pl241.frontend.Tokenizer;

class CFGBuildTest {
	public static void main(String[] args) {
		Tokenizer tokenizer = new Tokenizer();
		Parser parser = new Parser();

		try {
			
			final File folder = new File("inputs");
			final File destinationFolder = new File("Vis");

			for (final File fileEntry : folder.listFiles()) {
		        if (fileEntry.isFile()) {
		        	
		        	byte[] encoded = Files.readAllBytes(Paths.get(fileEntry.getPath()));
					String input = new  String(encoded, Charset.defaultCharset());

					System.out.println("Parsing " + fileEntry.getPath());

					tokenizer.tokenize(input.trim());
					ParseTreeNode root= parser.parse(tokenizer.getTokens());

					IRBuilderVisitor visitor = new IRBuilderVisitor();
					root.accept(visitor);
					System.out.println("Done with parsing");

					Program program = visitor.getProgram();

					// Reset data structures
					BasicBlock.reset();
					AbstractNode.resetNodeCounter();
					Variable.reset();

                    for (Function f : program.getFunctions()) {
                        f.insertBranches();
                        f.setBranchTargets();
                        f.removeUnreachableFlowEdges();
                    }

                    program.toSSAForm();
                    program.indexIR();

                    String testName = fileEntry.getName();
                    testName = testName.substring(0, testName.indexOf('.'));
                    program.visualize(destinationFolder + File.separator + testName + "_pass_1_ssa.dot", false);
                    program.visualizeDominatorTree(destinationFolder + File.separator + testName + "_dom_tree.dot");

		        }
		    }
			System.out.println("Parsing test passed");
			
			

		} catch (Exception e) {
			System.out.println(e.getMessage() );
			StackTraceElement[] trace = e.getStackTrace() ;
			for( StackTraceElement element: trace){
				System.out.println(element.getFileName() + ":" +element.getMethodName()+":"+element.getLineNumber());
				System.out.println("Parser test failed");
			}
		}

	}
}
