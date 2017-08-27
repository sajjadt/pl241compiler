package org.pl241.ir;

public class SourceLocation {
	
	private int lineNumber;
    
    private int colNumber;
	
	private String fileName;

	
	public SourceLocation(int lineNumber, int colNumber, String fileName) {
		this.lineNumber = lineNumber;
        this.colNumber = colNumber;
		this.fileName = fileName;
	}
	

	public String getFileName() {
		return fileName;
	}


	public int getLineNumber() {
		return lineNumber;
	}

    public int getColumnNumber() {
        return colNumber;
    }
    
    
}
