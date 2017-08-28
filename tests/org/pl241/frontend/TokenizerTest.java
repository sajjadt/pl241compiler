package org.pl241.frontend;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenizerTest {
	private static final Logger LOGGER = Logger.getLogger(ParserTest.class.getName());
	public static void main(String[] args) {
		Tokenizer tokenizer = new Tokenizer();
		try {
			final File folder = new File("inputs");
			for (final File fileEntry : folder.listFiles()) {

		        if (fileEntry.isFile()) {
		        	byte[] encoded = Files.readAllBytes(  Paths.get(fileEntry.getPath()));
					String input = new  String(encoded,  Charset.defaultCharset());

					LOGGER.log( Level.INFO, "Tokenizing " + fileEntry.getPath());
					tokenizer.tokenize(input.trim());

					for (Token tok : tokenizer.getTokens()) {
						LOGGER.log(Level.FINEST,"" + tok.token + "," + tok.sequence);
					}
		        }
		    }
			LOGGER.log(Level.INFO,"Tokenizer is done!");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			LOGGER.log(Level.SEVERE,"Tokenizer test failed");
		}
		

	}

}
