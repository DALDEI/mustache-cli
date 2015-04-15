package org.xmlsh.tools.mustache.cli.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.reflect.ReflectionObjectHandler;

/**
 * Uses Jacksonto support JSON scope objects
 */
public class JacksonObjectHandler extends ReflectionObjectHandler {
	static Logger mLogger = LogManager.getLogger();
	
    
    
    private final static  class DecoratedObjectNode extends AbstractMap<String,JsonNode> {
    	static Logger mLogger = LogManager.getLogger();

        private final ObjectNode mJso;

        private DecoratedObjectNode(ObjectNode jso) {
        	mLogger.entry(jso);
            mJso = jso;
        }

        @Override
        public String toString() {
            return writeJson(mJso);
        }

        @Override
        public   Set<Entry<String,JsonNode> >  entrySet() {
          return   new AbstractSet<Entry<String,JsonNode>>() {
                @Override
                public Iterator<Entry<String,JsonNode> > iterator() {
                    return mJso.fields();
                }
                @Override
                public int size() {
                    return 0;
                } 
            };
        }

        public JsonNode getNode() {
            return mJso;
        }

    }

    private final static class DecoratredJsonArray implements Iterable {
        private final ArrayNode mAnode;

        private DecoratredJsonArray(ArrayNode anode) {
        	mLogger.entry(anode);
            mAnode = anode;
        }

        @Override
        public Iterator<?> iterator() {
            return new DecoratedIterator(mAnode.iterator());
        }

        @Override
        public String toString() {
            return writeJson(mAnode);
        }

    }

    public static Object convertJson(JsonNode j) {
    	mLogger.entry(j);
        if (j.isObject()) {
            return
                    new DecoratedObjectNode( (ObjectNode) j ) ;
        }
        if( j.isArray())
            return new DecoratredJsonArray((ArrayNode) j );
        return j;

    }
    // Reads a JSON object and converts into a HashMap<String,Node>
    public static Object readJson(Reader r) throws JsonParseException,
    JsonMappingException, IOException {

        return getJsonObjectMapper().readValue(r,
                new TypeReference<HashMap<String, JsonNode>>() {
        });

    }

    // Reads a JSON object and converts into a HashMap<String,Node>
    public static Object readJson(String s) throws JsonParseException,
    JsonMappingException, IOException {

        return getJsonObjectMapper().readValue(s,
                new TypeReference<HashMap<String, JsonNode>>() {
        });

    }
    // Reads a JSON object and converts into a HashMap<String,Node>

    public static Object readJson(InputStream in) throws JsonParseException,
    JsonMappingException, IOException {
        return getJsonObjectMapper().readValue(in,
                new TypeReference<HashMap<String, JsonNode>>() {
        });

    }

    static volatile ObjectMapper _theObjectMapper = null;

    @Override
    public String stringify(Object object) {

        return writeObject(object);
    }
    
    public static String writeObject( Object object ){
        if( object == null )
            return null ;
        
        if( object instanceof DecoratedObjectNode ) 
           object = ((DecoratedObjectNode)object).getNode();
        if( object instanceof DecoratedIterator<?>.Element ) 
            object = ((DecoratedIterator<JsonNode>.Element)object).value;

        if( object instanceof ValueNode )
            object = valueNodeToObject( (ValueNode) object );
        if (object instanceof JsonNode)
            return writeJson((JsonNode) object);
        
        return object.toString();
    }

    public static String writeJson(final JsonNode jso)
    {
        try {
            return getJsonObjectMapper().writeValueAsString(jso);
        } catch (IOException e)
        {
            throw new MustacheException("Exception serializing Json Node: "
                    + jso.toString(), e);
        }
    }

    @Override
    public Object coerce(Object object) {
        if (object instanceof JsonNode) {
            final JsonNode jso = (JsonNode) object;
            if (jso.isArray() || jso.isObject() )
                return convertJson( jso );
                        
            if (jso.isMissingNode())
                return null;
            if (jso.isValueNode()) 
                return valueNodeToObject( (ValueNode) jso  );
        }
         
        return super.coerce(object);
    }


public static Object valueNodeToObject( ValueNode vn ){

	mLogger.entry(vn);
	Object ret = vn;

    if (vn.isBinary())
        ret =  ((BinaryNode) vn).binaryValue();
    else
    if (vn.isBoolean())
        ret= Boolean.valueOf(vn.asBoolean());
    if (vn.isNull())
        ret= null;
    if (vn.isNumber())
        ret= ((NumericNode) vn).numberValue();
    if (vn.isPojo())
        ret= ((POJONode) vn).getPojo();

    if (vn.isTextual())
        // Quote the string ?
        ret= ((TextNode) vn).textValue();
    // return writeJson(vn);
 
    return ret ;
}
	 public static ObjectMapper getJsonObjectMapper() {
	    	mLogger.entry();

	        // lets play and avoid syncronization
	        // on the off chance this is concurrent 2 mappers are created and one
	        // gets GC'd
	        if (_theObjectMapper == null) {

	            ObjectMapper mapper = new ObjectMapper();
	            // mapper.registerModule(new JaxbAnnotationModule());

	            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS,
	                    false);
	            // mapper.configure(DeserializationFeature. x , on );
	            mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
	            mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
	            mapper.configure(Feature.ALLOW_COMMENTS, true);
	            mapper.configure(Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
	            mapper.configure(Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
	            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
	                    false);
	            mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);

	            if (_theObjectMapper == null)
	                _theObjectMapper = mapper;

	        }
	        return _theObjectMapper;
	    }
	    
	    
}
