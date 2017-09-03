package org.pl241.ir;

public class IONode extends AbstractNode  {

    public enum IOType {
        READ,
        WRITE,
        WRITELINE
    };

    public IONode(IOType _type , AbstractNode operand) {
        super();
        type = _type;
        _readData = _writeData = false;
        if( type.equals(IOType.WRITE) ){
            operands.add(operand);
        }
        if ( type.equals(IOType.READ) ){
            _readData = true;
        } else if( type.equals(IOType.WRITE)) {
            _writeData = true;
        }
    }

    @Override
    public String toString() {
        String ret = super.toString() + " " + type ;
        if (type == IOType.WRITE)
            ret  += getOperandAtIndex(0).nodeId;
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
