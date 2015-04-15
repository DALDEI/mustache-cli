package org.xmlsh.tools.mustache.cli.api;


import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class DecoratedIterator<T> implements Iterator {
	static Logger mLogger = LogManager.getLogger( );
	
    Iterator<T> mIter ;
    private int mIndex =  0 ;
    DecoratedIterator(Iterator<T> iter ){
    	mLogger.entry(iter);
        mIter = iter ;
    }
    
    public class Element 
    {
        public final T value;
        public final boolean next = mIter.hasNext();
        public final String delim = next  ? "," : null ;
        public final boolean first ;
        public final boolean last = ! next ;
        public final int   index;
        
        
        Element(T v , int index ){ 
            value= v ;
            this.index = index ;
            first = index==0;
            
        }
        //@Override
        public T apply(String t) {
            return value ;
        }

        @Override
        public String toString() {
            return JacksonObjectHandler.writeObject(value);
        }

    }
    
    
    @Override
    public boolean hasNext() {
        return mIter.hasNext();
    }

    @Override
    public  Object next() {
        return new Element( mIter.next() , mIndex++  );
        
    }
}
