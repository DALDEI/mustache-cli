package org.xmlsh.tools.mustache.cli.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

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

    
    @Rule
    public StandardErrorStreamLog errLog = new StandardErrorStreamLog(LogMode.LOG_ONLY);
     
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
         
         
        String expected = "THIS IS A and that and [1, \"string\", {\"anon\":\"object\"}] and 1.2\n";
        assert out.equals(expected) ;
        
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
        assert out.equals(expected) ;
        
    }
}
