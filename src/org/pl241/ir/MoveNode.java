package org.pl241.ir;


import org.pl241.ra.Allocation;

// A transfer between two allocations
public class MoveNode extends AbstractNode implements NodeInterface {
    public MoveNode(Allocation from, Allocation to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return to.toString() + " = " + from.toString();
    }

    // Node interface implementation
    public boolean isExecutable() {
        return true;
    }
    public boolean hasOutputVirtualRegister() {
        return true;
    }
    public boolean visualize() {
        return true;
    }

    @Override
    public String printAllocation() {
        return to.toString() + " = " + from.toString();
    }

    public Allocation from;
    public Allocation to;
}
