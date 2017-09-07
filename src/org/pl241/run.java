package org.pl241;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.pl241.cg.DLX;
import org.pl241.cg.DLXCodeGenerator;
import org.pl241.cg.LowLevelProgram;
import org.pl241.frontend.*;
import org.pl241.frontend.Parser.ParseTreeNode;
import org.pl241.ir.Function;
import org.pl241.ir.IRBuilderVisitor;
import org.pl241.ra.RegisterAllocator;

public class run
{
	public static void main(String[] args) throws IOException
	{
		// Settings
        boolean visualize = true;
        boolean optimize = true;
        boolean allocateRegisters = true;
        boolean genCode = true;
        boolean execute = true;
        int numRegs = 16;
		String testName = "test008";
        String testPath = "inputs/test008.txt";

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
                program.visualize("Vis" + File.separator + testName + "_pass_0.dot", false);

            program.toSSAForm();
            program.indexIR();
            program.printVarInfo();

            if (visualize) {
                program.visualize("Vis" + File.separator + testName + "_pass_1_ssa.dot", false);
                program.visualizeDominatorTree("Vis" + File.separator + testName + "_dom_tree.dot");
            }

            if (optimize) {
                //program.cse();
                //program.indexIR();
                //if (visualize)
                //    program.visualize("Vis" + File.separator + testName + "_pass_2_cse.dot", false);

                program.copyPropagate();
                program.indexIR();
                if (visualize)
                    program.visualize("Vis" + File.separator + testName + "_pass_3_cp.dot", false);

            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        // Allocate registers and execute on DLX emulator
        try {
		    if (allocateRegisters) {
                RegisterAllocator allocator = new RegisterAllocator(numRegs);

                for (Function f : program.getFunctions()) {

                    // Allocates registers/spills virtual registers
                    // Returns splitted intervals with allocatin information
                    allocator.allocate(f);
                    allocator.toPhysical(f);

                    if (visualize)
                        program.visualize("Vis" + File.separator + testName + "_pass_4_ra.dot", false);

                    // Deconstructs SSA form
                    // Inserts additional moves if necessary
                    allocator.resolve(f);
                    if (visualize)
                        program.visualize("Vis" + File.separator + testName + "_pass_5_resolved.dot", false);
                }

                LowLevelProgram executable = new LowLevelProgram();
                executable.fromIRProgram(program, allocator);
                executable.visualize("Vis" + File.separator + testName + "_pass_6_llir.dot");

                if (genCode) {
                    DLXCodeGenerator codeGen = new DLXCodeGenerator(executable);
                    ArrayList<Integer> mem = codeGen.generateBinary();
                    for (int i =0 ;i < 20; i++) {
                        System.out.println(DLX.disassemble(mem.get(i)));
                    }

                    if (execute) {
                        DLX.load(mem);
                        System.out.println("MEM:" + mem);
                        System.out.println("Starting execution on DLX emulator...");
                        DLX.execute();
                    }
                }
            }
        } catch (Exception e) {
		    System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }

		System.out.println("All done...");
	}

}