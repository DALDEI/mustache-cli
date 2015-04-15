package org.xmlsh.tools.mustache.cli.api;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

final  class DecoratedObjectNode extends AbstractMap<String,JsonNode> {
	static Logger mLogger = LogManager.getLogger();

    private final ObjectNode mJso;

    DecoratedObjectNode(ObjectNode jso) {
    	mLogger.entry(jso);
        mJso = jso;
    }

    @Override
    public String toString() {
        return JacksonObjectHandler.writeJson(mJso);
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