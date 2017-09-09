package org.pl241.ir;

public class IONode extends AbstractNode  {

    public enum IOType {
        READ,
        WRITE,
        WRITELINE;

        @Override
        public String toString() {
            switch(this) {
                case READ: return "read";
                case WRITE: return "write";
                case WRITELINE: return "writeln";
                default: throw new IllegalArgumentException();
            }
        }
    }

    public IONode() {
        super();
    }

    public IONode(IOType type , AbstractNode operand) {
        super();
        setParams(type, operand);
    }

    public void setParams(IOType type , AbstractNode operand) {
        this.type = type;
        _readData = _writeData = false;
        if (type.equals(IOType.WRITE)) {
            operands.add(operand);
        }
        if (type.equals(IOType.READ)) {
            _readData = true;
        } else if( type.equals(IOType.WRITE)) {
            _writeData = true;
        }
    }

    @Override
    public String toString() {
        String ret = super.toString() + " ";
        if (type == IOType.WRITE)
            ret  += (type + "(" + getOperandAtIndex(0).getOutputOperand() + ")");
        else
            ret  += (type + "()");
        return ret;
    }

    public boolean readData() {
        return this._readData;
    }
    public boolean writeData() {
        return this._writeData;
    }

    public String getOutputOperand() {
        if( this._readData ){
            return super.nodeId;
        } else {
            return null;
        }
    }

	private boolean _readData ;
	private boolean _writeData ;
	public IOType type;


    @Override
    public boolean hasOutputRegister() {
        return _readData;
    }
    @Override
    public boolean isExecutable() {
        return true;
    }
}
