package org.pl241;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.pl241.cg.DLX;
import org.pl241.cg.DLXCodeGenerator;
import org.pl241.frontend.*;
import org.pl241.frontend.Parser.ParseTreeNode;
import org.pl241.ir.IRBuilderVisitor;
import org.pl241.ir.Function;
import org.pl241.ir.Program;
import org.pl241.ra.RegisterAllocator;

public class run
{
	public static void main(String[] args) throws IOException
	{
		// Settings
        boolean visualize = true;
        boolean optimize = true;
        boolean execute = false;
        int numRegs = 8;

		String testName = "test005";
        String testPath = "inputs/test005.txt";

		byte[] encoded = Files.readAllBytes(Paths.get(testPath));
		String input = new String(encoded, Charset.defaultCharset());

		Tokenizer tokenizer = new Tokenizer();
		Parser parser = new Parser();
		IRBuilderVisitor visitor = new IRBuilderVisitor();

		try {
			tokenizer.tokenize(input.trim());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		try {
            ParseTreeNode root = parser.parse(tokenizer.getTokens());
            root.accept(visitor);
            Program program = visitor.getProgram();
            // to SSE form
            //program.process();

            if (visualize)
                program.visualize("cfgs" + File.separator + testName + "_a.dot");

            for (Function f : program.getFunctions()) {
                // both should use the same layout ordering
                f.indexIR();
                f.addMissingBranches();
            }
            if (visualize)
                program.visualize("cfgs" + File.separator + testName + "_b.dot");

            if (optimize) {
                program.copyPropagate();
                if (visualize)
                    program.visualize("cfgs" + File.separator + testName + "_cp.dot");
                program.cse();

                for (Function f : program.getFunctions()) {
                    // both should use the same layout ordering
                    f.indexIR();
                    f.addMissingBranches();
                }
                if (visualize) {
                    program.visualize("cfgs" + File.separator + testName + "_cse.dot");
                    program.visualizeDominatorTree("cfgs" + File.separator + testName + "_dom_tree.dot");
                }
            }

            for (Function f : program.getFunctions()) {
                RegisterAllocator allocator = new RegisterAllocator(numRegs, f);
                allocator.allocate();
                allocator.resolve();
            }


            DLXCodeGenerator generator = new DLXCodeGenerator(program);
            ArrayList<Integer> mem = generator.generateSampleCode();

            if (execute) {
                DLX.load(mem);
                System.out.println("MEM:" + mem);
                System.out.println("Executing on DLX");
                DLX.execute();
            }
        }
        catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done with parsing");

	}
}