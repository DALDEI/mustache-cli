package org.xmlsh.tools.mustache.cli.functions;

import java.io.IOException;
import java.io.StringReader;
import java.util.function.Function;

import org.xmlsh.tools.mustache.cli.api.JacksonObjectHandler;
import org.xmlsh.tools.mustache.cli.api.MustacheContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.TemplateFunction;

public class JsonFunctions {

	
	/*
	 * This function doesnt work properly ... 
	 */
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
		public Object apply(String text) {

			try {
				JsonNode node = 
				JacksonObjectHandler.getJsonObjectMapper().readTree(text);
				return JacksonObjectHandler.writeJson(node)
						;
			}catch (IOException e) {
				throw new MustacheException("Invalid JSON", e);
			}
			
			
		}
	}
	static class JsonQuoteFunction implements Function<String,Object> {

		MustacheContext context;
		JsonQuoteFunction(MustacheContext c ) { 
			context = c ;
		}
		@Override
		public Object apply(String text) {

			try {
				return JacksonObjectHandler.getJsonObjectMapper().writeValueAsString(text);
			}catch (IOException e) {
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
	public static Object quoteFunction(MustacheContext c) { 
		return new JsonQuoteFunction( c );
	}
	
}
