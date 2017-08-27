package org.pl241.frontend;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.pl241.frontend.Parser.ParseTreeNode;
import org.pl241.ir.BuildIRVisitor;

public class ParserTest {
	public static void main(String[] args) {
		Tokenizer tokenizer = new Tokenizer();
		Parser parser = new Parser();

		//String input = "main var a,b,y,i,d; var z,d; " + "{" + "	let i <- 1;"
		//		+ "	od;" + "	let d <- y + b" + "}.";
		//
		try {
			
			final File folder = new File("D:\\advComp\\inputs");
			for (final File fileEntry : folder.listFiles()) {
		        if (fileEntry.isFile()) {
		        	
		        	byte[] encoded = Files.readAllBytes(  Paths.get(fileEntry.getPath()));
					String input = new  String(encoded,  Charset.defaultCharset());

					
					System.out.println("Parsing " + fileEntry.getPath());
					
					
					tokenizer.tokenize(input.trim());
					ParseTreeNode root= parser.parse(tokenizer.getTokens());
					
					root.accept(new BuildIRVisitor());
					System.out.println("Done with parsing");
					
					
		        }
		    }
			System.out.println("Parsing test passed");
			
			

		} catch (Exception e) {
			System.out.println(e.getMessage() );
			StackTraceElement[] trace = e.getStackTrace() ;
			for( StackTraceElement element: trace){
				System.out.println(element.getFileName() + ":" +element.getMethodName()+":"+element.getLineNumber());
				System.out.println("Parser test failed");
			}
		}

	}
}
