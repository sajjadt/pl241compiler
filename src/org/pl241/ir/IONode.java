package org.pl241.ir;

import java.util.HashMap;
import java.util.Map;

public class IONode extends AbstractNode  {

	public enum IOType {
		READ,
		WRITE , 	
		WRITELINE
	};
	
	private boolean _readData ;
	private boolean _writeData ;
	public IOType type;
		
	public IONode(IOType _type , AbstractNode operand) {

		super(_type.toString());
		type = _type;
		_readData = _writeData = false; 
		if( type.equals(IOType.WRITE) ){
			operands.add(operand);
		}
		if ( type.equals(IOType.READ) ){
			_readData = true;
		} else if( type.equals(IOType.WRITE)) {
			_writeData = true;;
		}
		
	}
	
	@Override
	public String toString() {
		return uniqueLabel + ": "  ;
	}
	
	public boolean readData() {
		return this._readData;
	}
	public boolean writeData() {
		return this._writeData ;
	}
	
	@Override
	public String getOutputOperand() {
		if( this._readData ){
			return super.uniqueLabel;
		} else {
			return null; 
		}
	}
}
