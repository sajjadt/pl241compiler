package org.pl241.frontend;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TokenizerTest {

	public static void main(String[] args) {
		Tokenizer tokenizer = new Tokenizer();

		//String input = " main var a,b,y,i,d;" 
		//		+ "{" 
		// + "	let i <- 1;"
		//+ "	while i > 0 do " 
		//		+ "		let y <- 1;"
		//		+ "		let i <- i + 1"
		//		+ "	od;"
		//		+ "	let d <- y + b;" 
		//		+ "}.";
		try {
			
			final File folder = new File("D:\\advComp\\inputs");
			for (final File fileEntry : folder.listFiles()) {
		        if (fileEntry.isFile()) {
		        	
		        	byte[] encoded = Files.readAllBytes(  Paths.get(fileEntry.getPath()));
					String input = new  String(encoded,  Charset.defaultCharset());

					
					System.out.println("Tokenizing " + fileEntry.getPath());
					tokenizer.tokenize(input.trim());

					for (Token tok : tokenizer.getTokens()) {
						System.out.println("" + tok.token + "," + tok.sequence);
					}
		        }
		    }
			System.out.println("Tokenize test passed");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Tokenizer test failed");
		}
		

	}

}
