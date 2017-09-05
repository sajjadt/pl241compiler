package org.pl241.cg;

import org.pl241.Program;
import org.pl241.ir.AbstractNode;
import org.pl241.ir.Function;
import org.pl241.ra.RegisterAllocator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class LowLevelProgram {

    public LowLevelProgram() {
        lowLevelIR = new HashMap<>();
        this.program = null;
    }

    public List<Integer> generateExecutable () {
        return new ArrayList<>();
    }

    public void fromIRProgram(Program program, RegisterAllocator allocator) {
        this.program = program;

        for(Function f: program.getFunctions()) {
            List<Instruction> instructions = new ArrayList<>();

            for (AbstractNode node: f.getNodesInLayoutOrder()) {
                instructions.addAll(Instruction.fromIRNode(node, allocator));
            }
            lowLevelIR.put(f.name, instructions);
        }
    }

    private Program program;
    private HashMap<String, List<Instruction>> lowLevelIR;

    public void visualize(String path) throws IOException {
        File file = new File(path);
        file.createNewFile();

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("digraph {");
            for (Function function : program.getFunctions()) {
                visualizeFunction(function, writer);
            }
            writer.println("}");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void visualizeFunction(Function function, PrintWriter pw) {

        //pw.println("subgraph " + "cluster" + function. + " {");
        pw.println("label=" + function.getName() );
        pw.println("labelloc=\"t\";");
        pw.println("fontsize=18;");

        pw.println("rankdir=\"TD\"");

        pw.print("BB"  + " [shape=record label=\"{");
        pw.print( "Function " + function.name + "\n");

        for (Instruction n: lowLevelIR.get(function.name)) {
            pw.print('|' + n.toString());
        }

        pw.print("}\" ] " + "\n");

//        pw.println("}");

    }

}
