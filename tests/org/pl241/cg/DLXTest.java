package org.pl241.cg;

import java.io.IOException;
import java.util.ArrayList;

import org.pl241.cg.DLX;
import org.pl241.cg.DLXCodeGenerator;


public class DLXTest {
	

	public static void main(String[] args){
		DLXCodeGenerator generator = new DLXCodeGenerator(null);
		ArrayList<Integer> program = generator.generateSampleCode() ;
		DLX.load( program );
		try {
			DLX.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
