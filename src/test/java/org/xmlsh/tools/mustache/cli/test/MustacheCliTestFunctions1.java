package org.xmlsh.tools.mustache.cli.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.LogMode;
import org.junit.contrib.java.lang.system.StandardErrorStreamLog;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;
import org.xmlsh.tools.mustache.cli.main.Main;
import org.xmlsh.tools.mustache.cli.main.Main.UsageException;

public class MustacheCliTestFunctions1 {
	
	static {
				//System.setProperty("org.apache.logging.log4j.level","TRACE");
	}

    
    @Rule
    public StandardErrorStreamLog errLog = new StandardErrorStreamLog();
     
    @Rule
    public StandardOutputStreamLog  outLog = new StandardOutputStreamLog(LogMode.LOG_ONLY);
    
    @Before
    public void before() {
       
    }
    @Test
    public void test1() throws Exception {
        Main main = new Main( new String[] {
           "-t" ,
                "{{#json}}{ 'parsed' : { 'object' : [ 1 , 'foo' , '{{test}}']} } {{/json}}\n" , 
             "test=bar" 
                }
        );
           main.run();

         String out = outLog.getLog();
         
         
        String expected = "{\"parsed\":{\"object\":[1,\"foo\",\"bar\"]}}\n";
        assertEquals("Expanded template", expected, out);
        
    }
    
    @Test
    public void test2() throws Exception {
        Main main = new Main( new String[] {
           "-t" ,
                "{{#json}}{ 'parsed' : {{parsed}} , 'test' : '{{test}}' } {{/json}}\n" , 
            "-j" ,
           "{ 'parsed' :{ 'object' : [ 1 , 'foo' ]}} ",  
           "test=bar" }
        );
           main.run();

         String out = outLog.getLog();
         
         
        String expected = "{\"parsed\":{\"object\":[1,\"foo\"]},\"test\":\"bar\"}\n";
        assertEquals("Expanded template", expected, out);
        
    }
    
    @Test
    public void test3() throws Exception {
        Main main = new Main( new String[] {
           "-t" ,
                "{{#reparse}}parsed.{{obj}}{{/reparse}}\n" , 
            "-j" ,
           "{ 'parsed' :{ 'object' : [ 1 , 'foo' ]}} ",  
           "obj=object" }
        );
           main.run();

         String out = outLog.getLog();
         
         
        String expected = "{\"parsed\":{\"object\":[1,\"foo\"]},\"test\":\"bar\"}\n";
        assertEquals("Expanded template", expected, out);
        
    }
    
}
