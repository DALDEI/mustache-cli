package org.xmlsh.tools.mustache.cli.api;

import java.util.Iterator;

import com.fasterxml.jackson.databind.node.ArrayNode;

final class DecoratredJsonArray implements Iterable {
    private final ArrayNode mAnode;

    DecoratredJsonArray(ArrayNode anode) {
    	JacksonObjectHandler.mLogger.entry(anode);
        mAnode = anode;
    }

    @Override
    public Iterator<?> iterator() {
        return new DecoratedIterator(mAnode.iterator(), "[" , "," , "]" );
    }

    @Override
    public String toString() {
        return JacksonObjectHandler.writeJson(mAnode);
    }

}