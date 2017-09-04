package org.pl241;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.pl241.cg.DLX;
import org.pl241.cg.DLXCodeGenerator;
import org.pl241.frontend.*;
import org.pl241.frontend.Parser.ParseTreeNode;
import org.pl241.ir.Function;
import org.pl241.ir.IRBuilderVisitor;
import org.pl241.ra.LiveInterval;
import org.pl241.ra.RegisterAllocator;

public class run
{
	public static void main(String[] args) throws IOException
	{
		// Settings
        boolean visualize = true;
        boolean optimize = false;
        boolean allocateRegisters = false;
        boolean genCode = false;
        boolean execute = false;
        int numRegs = 16;
		String testName = "test003";
        String testPath = "inputs/test003.txt";

        // Tokenize the input
		byte[] encoded = Files.readAllBytes(Paths.get(testPath));
		String input = new String(encoded, Charset.defaultCharset());
		Tokenizer tokenizer = new Tokenizer();
        Program program;
		try {
			tokenizer.tokenize(input.trim());
		} catch (Exception e) {
            System.out.println(e.getMessage());
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

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        // Process and optimize the program
        try {
            for (Function f : program.getFunctions()) {
                f.insertBranches();
                f.setBranchTargets();
                f.removeUnreachableFlowEdges();
            }

            if (visualize)
                program.visualize("Vis" + File.separator + testName + "_pass_0.dot");

            program.toSSAForm();
            program.indexIR();

            if (visualize) {
                program.visualize("Vis" + File.separator + testName + "_pass_1_ssa.dot");
                program.visualizeDominatorTree("Vis" + File.separator + testName + "_dom_tree.dot");
            }


            program.printVarInfo();


            if (optimize) {

                program.copyPropagate();
                if (visualize)
                    program.visualize("Vis" + File.separator + testName + "_pass_2_cp.dot");

                program.cse();
                if (visualize)
                    program.visualize("Vis" + File.separator + testName + "_pass_3_cse.dot");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        // Allocate registers
        try {
		    if (allocateRegisters) {
                RegisterAllocator allocator = new RegisterAllocator(numRegs);

                for (Function f : program.getFunctions()) {

                    // Allocates registers/spills virtual registers
                    // Returns splitted intervals with allocatin information
                    HashMap<String, ArrayList<LiveInterval>> intervals = allocator.allocate(f);

                    // Works on intervals
                    // Insert additional moves if necessary into f basic blocks
                    allocator.resolve(f, intervals);
                    allocator.toPhysical(f, intervals);
                }

                if (visualize)
                    program.visualize("Vis" + File.separator + testName + "_pass_4_ra.dot");
            }

            ArrayList<Integer> mem = new ArrayList<>();
            if (allocateRegisters && genCode) {
                DLXCodeGenerator generator = new DLXCodeGenerator(program);
                mem = generator.generateProgram();
                for (int i =0 ;i < 20; i++) {
                    System.out.println(DLX.disassemble(mem.get(i)));
                }
            }
            if (genCode && execute) {
                DLX.load(mem);
                System.out.println("MEM:" + mem);
                System.out.println("Executing on DLX");
                DLX.execute();
            }

        }
        catch (Exception e) {
		    System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }
		System.out.println("Done...");
	}
}