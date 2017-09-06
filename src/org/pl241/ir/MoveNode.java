package org.pl241.ir;


import org.pl241.ra.Allocation;

public class MoveNode extends AbstractNode {
    public MoveNode(Allocation from, Allocation to) {
        this.from = from;
        this.to = to;
    }

    public Allocation from;
    public Allocation to;

    @Override
    public String toString() {
        return to.toString() + " = " + from.toString();
    }
}
