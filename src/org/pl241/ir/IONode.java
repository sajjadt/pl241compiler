package org.pl241.ir;

import java.util.HashMap;
import java.util.Map;

public class IONode extends AbstractNode  {
	public static enum IOType {
		READ,
		WRITE , 	
		WRITELINE};
	
	private static Map<IOType,String> ioMap;
	private boolean _readData ;
	private boolean _writeData ;
	
	static {
		ioMap = new HashMap<IOType, String >();
		ioMap.put(IOType.READ, "RDD" );
		ioMap.put(IOType.WRITE, "WRD" );
		ioMap.put(IOType.WRITELINE, "WRL" );
		}	
	
		
	public IONode(IOType type , AbstractNode operand) {
		
		super(ioMap.get(type));
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
	public String toString(){
		return  "lindex " + sourceLocation +  " " + super.uniqueLabel + ": H  " + operands;
	}
	
	public boolean readData(){
		return this._readData;
	}
	public boolean writeData(){
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
