package org.xmlsh.tools.mustache.cli.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
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
import com.github.mustachejava.Binding;
import com.github.mustachejava.Code;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.TemplateContext;
import com.github.mustachejava.reflect.ReflectionObjectHandler;
import com.github.mustachejava.reflect.guards.ClassGuard;
import com.github.mustachejava.util.Wrapper;

/**
 * Uses Jacksonto support JSON scope objects
 */
public class JacksonObjectHandler extends ReflectionObjectHandler {

    public static Object readJson(Reader r) throws JsonParseException,
            JsonMappingException, IOException {

        return getJsonObjectMapper().readValue(r,
                new TypeReference<HashMap<String, JsonNode>>() {
                });

    }

    public static Object readJson(String s) throws JsonParseException,
            JsonMappingException, IOException {

        return getJsonObjectMapper().readValue(s,
                new TypeReference<HashMap<String, JsonNode>>() {
                });

    }

    public static Object readJson(InputStream in) throws JsonParseException,
            JsonMappingException, IOException {
        return getJsonObjectMapper().readValue(in,
                new TypeReference<HashMap<String, JsonNode>>() {
                });

    }

    static volatile ObjectMapper _theObjectMapper = null;

    @Override
    public String stringify(Object object) {

        if (object instanceof JsonNode)
            return writeJson((JsonNode) object);
        return super.stringify(object);
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

    @SuppressWarnings("serial")
    @Override
    public Object coerce(Object object) {
        if (object instanceof JsonNode) {
            final JsonNode jso = (JsonNode) object;
            if (jso.isArray()) {
                final ArrayNode anode = ((ArrayNode) jso);
                return new ArrayList<JsonNode>() {
                    {
                        anode.elements().forEachRemaining((e) -> add(e));
                    }
                };

            }
            if (jso.isObject()) {
                @SuppressWarnings("serial")
                HashMap<String, JsonNode> map = new HashMap<String, JsonNode>() {
                    {
                        jso.fields().forEachRemaining(
                                e -> put(e.getKey(), e.getValue()));
                    }

                    @Override
                    public String toString() {
                        return writeJson(jso);
                    }

                };

                // return map; // super.coerce(map);
                return map;
            }
            if (jso.isMissingNode())
                return null;

            if (jso.isValueNode()) {
                ValueNode vn = ((ValueNode) jso);
                if (vn.isBinary())
                    return ((BinaryNode) vn).binaryValue();
                if (vn.isBoolean())
                    return Boolean.valueOf(vn.asBoolean());
                if (vn.isNull())
                    return null;
                if (vn.isNumber())
                    return ((NumericNode) vn).numberValue();
                if (vn.isPojo())
                    return ((POJONode) vn).getPojo();

                if (vn.isTextual())
                    // Quote the string ?
                    return ((TextNode) vn).textValue();
                // return writeJson(vn);
            }
        }
        return super.coerce(object);
    }

    public static ObjectMapper getJsonObjectMapper() {

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

            /*
             * Test code needs to go on AWS
             * mapper.addMixInAnnotations(com.amazonaws
             * .services.ec2.model.EbsBlockDevice.class,
             * IgnoreVolumeTypeEnum.class);
             */

            // other completely global configurations

            if (_theObjectMapper == null)
                _theObjectMapper = mapper;

        }
        return _theObjectMapper;
    }

}
