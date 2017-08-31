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
        int numRegs = 16;
		String testName = "minssa";
        String testPath = "inputs/minssa.txt";

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
            if (visualize)
                program.visualize("Vis" + File.separator + testName + "_pass_0.dot");

        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Process and optimize the program
        try {
            for (Function f : program.getFunctions()) {
                // both should use the same layout ordering
                f.indexIR();
                f.addMissingBranches();
                f.setBranchTargets();
            }

            if (visualize)
                program.visualize("Vis" + File.separator + testName + "_pass_1.dot");

            program.toSSAForm();

            if (visualize)
                program.visualize("Vis" + File.separator + testName + "_pass_ssa.dot");

            if (optimize) {
                program.copyPropagate();
                if (visualize)
                    program.visualize("Vis" + File.separator + testName + "_pass_2_cp.dot");
                program.cse();


                if (visualize) {
                    program.visualize("Vis" + File.separator + testName + "_pass_3_cse.dot");
                    program.visualizeDominatorTree("Vis" + File.separator + testName + "_dom_tree.dot");
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

            for (int i =0 ;i < 20; i++) {
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