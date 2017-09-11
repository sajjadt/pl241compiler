package org.pl241.ir;

public interface NodeInterface {
    // Determines whether this node matches a node which does computation
    boolean isExecutable();
    // Determines whether this node updates a virtual register
    boolean hasOutputVirtualRegister();
    // Returns textual representation of output operand
    String getOutputVirtualReg();
    // Determines whether this node should be visualize in CFGs.
    // Some helper nodes better not to be included in CFGs for clarity.
    boolean visualize();
}

