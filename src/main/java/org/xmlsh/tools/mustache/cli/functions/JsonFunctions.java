package org.xmlsh.tools.mustache.cli.functions;

import java.io.IOException;
import java.io.StringReader;
import java.util.function.Function;

import org.xmlsh.tools.mustache.cli.api.MustacheContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.TemplateFunction;

public class JsonFunctions {

	static class JsonArrayFunction implements TemplateFunction {

		MustacheContext context;
		JsonArrayFunction(MustacheContext c ) { 
			context = c ;
		}

		@Override
		public String apply(String t) {
			
			StringBuilder sb = new StringBuilder();
			sb.append("[ ");
			
			if( t != null ){
				sb.append( t );
				
			}
			sb.append( " ]");
			return sb.toString();
			
			
		}
		
	}
	
	static class JsonFunction implements Function<String,Object> {

		MustacheContext context;
		JsonFunction(MustacheContext c ) { 
			context = c ;
		}
		@Override
		public Object apply(String t) {

			try {
				return context.parseJson( new StringReader(t) );
			} catch (IOException e) {
				throw new MustacheException("Invalid JSON", e);
			}
			
			
		}
	}

	public static Object jsonFunction(MustacheContext c) { 
		return new JsonFunction( c );
	}
	public static Object arrayFunction(MustacheContext c) { 
		return new JsonArrayFunction( c );
	}
	
}
