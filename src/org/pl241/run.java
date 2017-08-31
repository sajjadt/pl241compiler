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
import org.pl241.ra.RegisterAllocator;

public class run
{
	public static void main(String[] args) throws IOException
	{
		// Settings
        boolean visualize = true;
        boolean optimize = true;
        boolean execute = true;
        int numRegs = 31;
		String testName = "test001";
        String testPath = "inputs/test001.txt";

        // Tokenize the input
		byte[] encoded = Files.readAllBytes(Paths.get(testPath));
		String input = new String(encoded, Charset.defaultCharset());
		Tokenizer tokenizer = new Tokenizer();
        Program program;
		try {
			tokenizer.tokenize(input.trim());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
        // Parse the input and create IR representation
		try {
            Parser parser = new Parser();
            IRBuilderVisitor visitor = new IRBuilderVisitor();
            ParseTreeNode root = parser.parse(tokenizer.getTokens());
            root.accept(visitor);

            program = visitor.getProgram();
            program.process();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // Process and optimize the program
        try {
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
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // Allocate registers
        try {
            for (Function f : program.getFunctions()) {
                RegisterAllocator allocator = new RegisterAllocator(numRegs, f);
                allocator.allocate(true);
                //allocator.resolve();
                allocator.printAllocation(f);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // Generate program and execute
        try {
            DLXCodeGenerator generator = new DLXCodeGenerator(program);
            ArrayList<Integer> mem = generator.generateProgram();

            for (int i =0 ;i < 12; i++) {
                System.out.println(DLX.disassemble(mem.get(i)));
            }
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