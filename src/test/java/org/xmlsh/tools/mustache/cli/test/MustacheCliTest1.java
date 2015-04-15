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

public class MustacheCliTest1 {
	
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
                "THIS {{is}} A {{nested.this}} and {{nested.template.array }} and {{nested.template.number}}\n" , 
             "-j" , 
                "{ 'is': 'IS' , 'nested' : " +
                   "{ 'this' : 'and that'  , " +
                    "  'template' : " + 
                      "{ 'string' : 'STRING' , 'number' :       1.2 , " +
                          " 'array' : [ 1, 'string' , { 'anon' : 'object'  } ] }}} "} );
          
           main.run();

         String out = outLog.getLog();
         
         
        String expected = "THIS IS A and that and [1,\"string\",{\"anon\":\"object\"}] and 1.2\n";
        assertEquals("Expanded template", expected, out);
        
    }
    
    @Test
    public void test2() throws Exception {
        Main main = new Main( new String[] {
           "-t" ,
                "a={{a}},b={{b}},c.d={{c.d}}",
            "a=A",
            "b=B",
            "c.d=C.D" });
           main.run();

         String out = outLog.getLog();
        String expected = "a=A,b=B,c.d=C.D";
        assertEquals( "Expanded Template" , expected,out);
        
    }
    @Test
    public void test3() throws Exception {
        Main main = new Main( new String[] {
           "-t" ,
                "{{foo.bar}}",
           "-j",
            "{ \"foo\" : {  \"bar\" : [ 1 , \"hi\" ] } }" } );
           main.run();

         String out = outLog.getLog();
        String expected = "[1,\"hi\"]";
        assertEquals( "Expanded Template" , expected,out);
        
    }
    
    
    @Test
    public void test4() throws Exception {
        Main main = new Main( new String[] {
           "-t" ,
                "[{{#foo.bar}}{{value}}{{delim}}{{/foo.bar}}]",
           "-j",
            "{ \"foo\" : {  \"bar\" : [ \"first\" , 2 , \"third\" , 4 , { \"fifth\" : 5 } ] } }" } );
           main.run();

         String out = outLog.getLog();
        String expected = "[first,2,third,4,{\"fifth\":5}]";
        assertEquals( "Expanded Template" , expected,out);
        
    }
 
    @Test
    public void test5() throws Exception {
        Main main = new Main( new String[] {
           "-t" ,
                "{{#foo.bar}}{{#first}}[{{/first}}{{index}}|{{first}}|{{next}}|{{last}}|{{value}}|{{.}}|{{fifth}}|{{value.fifth}}{{#next}},{{/next}}{{#last}}]{{/last}} {{/foo.bar}}]",
           "-j",
            "{ \"foo\" : {  \"bar\" : [ \"first\" , 2 , \"third\" , 4 , { \"fifth\" : 5 } ] } }" } );
           main.run();

         String out = outLog.getLog();
        String expected = "[0|true|true|false|first|first||, 1|false|true|false|2|2||, 2|false|true|false|third|third||, 3|false|true|false|4|4||, 4|false|false|true|{\"fifth\":5}|{\"fifth\":5}||5] ]";
        assertEquals( "Expanded Template" , expected,out);
        
    }
 
    
    
    
}
