package org.pl241.ir;

import org.pl241.frontend.Parser;
import org.pl241.frontend.Parser.ParseTreeNode;
import org.pl241.frontend.Tokenizer;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class IRBuilderTest {

	private static final Logger LOGGER = Logger.getLogger(IRBuilderTest.class.getName());
	public static void main(String[] args) {
		Tokenizer tokenizer = new Tokenizer();
		Parser parser = new Parser();

		LOGGER.addHandler(new ConsoleHandler());
		try {
			final File folder = new File("inputs");
			for (final File fileEntry : folder.listFiles()) {
		        if (fileEntry.isFile()) {
		        	
		        	byte[] encoded = Files.readAllBytes(  Paths.get(fileEntry.getPath()));
					String input = new  String(encoded,  Charset.defaultCharset());

					LOGGER.log( Level.INFO,"Parsing " + fileEntry.getPath());
					tokenizer.tokenize(input.trim());
					ParseTreeNode root= parser.parse(tokenizer.getTokens());

					root.accept(new IRBuilderVisitor());
		        }
		    }
			LOGGER.log( Level.INFO,"Parsing test passed");

		} catch (Exception e) {
			System.out.println(e.getMessage() );
			StackTraceElement[] trace = e.getStackTrace() ;
			for( StackTraceElement element: trace){
				System.out.println(element.getFileName() + ":" +element.getMethodName()+":"+element.getLineNumber());
				LOGGER.log( Level.SEVERE,"Parser test failed");
			}
		}

	}
}
