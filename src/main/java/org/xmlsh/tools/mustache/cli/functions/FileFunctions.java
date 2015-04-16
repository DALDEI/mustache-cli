package org.xmlsh.tools.mustache.cli.functions;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.xmlsh.tools.mustache.cli.api.JacksonObjectHandler;
import org.xmlsh.tools.mustache.cli.api.MustacheContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.TemplateFunction;

public class FileFunctions {

	static class IncludeFunction implements Function<String,String> {

		MustacheContext context;
		IncludeFunction(MustacheContext c ) { 
			context = c ;
		}

		@Override
		public String apply(String t) {
			if( t == null )
				return null;
			try {
				return new String(Files.readAllBytes(
				  context.resolveFile(t).toPath() ) , context.getInputEncoding() );
			} catch (IOException e) {
				throw new MustacheException("Exception reading file:" + t , e);
			}
			
			
		}
		
	}

	static class LinesFunction implements Function<String,List<String>> {

		MustacheContext context;
		LinesFunction(MustacheContext c ) { 
			context = c ;
		}

		@Override
		public List<String> apply(String t) {
			if( t == null )
				return null;
			try {
				return Files.readAllLines(
				  context.resolveFile(t).toPath() , 
				  Charset.forName(context.getInputEncoding() ));
			} catch (IOException e) {
				throw new MustacheException("Exception reading file:" + t , e);
			}
			
			
		}
		
	}
	
	/*
	 * Surounds the body by the curent em and returns it for reparsing
	 */
	
	static class ReparseFunction implements TemplateFunction {

		MustacheContext context;
		ReparseFunction(MustacheContext c ) { 
			context = c ;
		}

		@Override
		public String apply(String t) {

			if( t == null )
				return null;
			
			StringBuilder sb = new StringBuilder();
			sb.append( context.getDelimStart() );
				sb.append( t );
			sb.append( context.getDelimEnd() )
;			return sb.toString();
			
			
		}
		
	}
	
	
	public static Object includeFunction(MustacheContext c) { 
		return new IncludeFunction( c );
	}
	public static Object linesFunction(MustacheContext c) { 
		return new IncludeFunction( c );
	}
	public static Object reparseFunction(MustacheContext c) { 
		return new ReparseFunction( c );
	}
}
