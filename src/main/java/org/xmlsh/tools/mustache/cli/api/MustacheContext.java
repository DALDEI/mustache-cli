package org.xmlsh.tools.mustache.cli.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Properties;

import org.xmlsh.tools.mustache.cli.main.Main;
import org.xmlsh.tools.mustache.cli.main.Main.Encoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;

public class MustacheContext {
    private DefaultMustacheFactory mf;
    private ArrayList<Object> scope = new ArrayList<>();
    private Reader template;
    private Writer output;
    private String template_name;
    private String delimStart = "{{";
    private String delimEnd = "}}";
    private Encoder<String, Writer> encoder;
    public String getDelimEnd() {
        return delimEnd;
    }

    public String getDelimStart() {
        return delimStart;
    }

    public Encoder<String, Writer> getEncoder() {
        return encoder;
    }


    public Writer getOutput() {
        return output;
    }

    public ArrayList<Object> getScope() {
        return scope;
    }

    public Reader getTemplate() {
        return template;
    }

    public String getTemplate_name() {
        return template_name;
    }

    public void setDelimEnd(String delimEnd) {
        this.delimEnd = delimEnd;
    }

    public void setDelimStart(String delimStart) {
        this.delimStart = delimStart;
    }

    public void setEncoder(Encoder<String, Writer> encoder) {
        this.encoder = encoder;
    }

    public void setOutput(Writer output) {
        this.output = output;

    }

    public void setScope(ArrayList<Object> scope) {
        this.scope = scope;

    }

    public void setTemplate(Reader template) {
        this.template = template;

    }

    public void setTemplate_name(String template_name) {
        this.template_name = template_name;

    }
     

    public DefaultMustacheFactory getMustacheFactory() {
        if (mf == null){
            mf = new DefaultMustacheFactory() {

                @Override
                public void encode(String value, Writer writer) {
                    try {
                        getEncoder().accept(value, writer);
                    } catch (IOException e) {
                        throw new MustacheException(
                                "Failed to encode " + value, e);
                    }
                }
            };
            mf.setObjectHandler(new JacksonObjectHandler());
        }
        return mf;
    }

   public Writer execute() {
       if( encoder == null )
          encoder =    (s,w) -> w.write(s) ; 


       Mustache mustache = getMustacheFactory().compile(getTemplate(),
                getTemplate_name() == null ? "main" : getTemplate_name(),
                getDelimStart(), getDelimEnd());

        return mustache.execute(getOutput(), getScope().toArray());
    }

   
   public Object parseJson(Reader r) throws JsonProcessingException,
           IOException {
       return JacksonObjectHandler.readJson(r);

   }

   private Object parseJson(String s) throws JsonProcessingException,
           IOException {

       return JacksonObjectHandler.readJson(s );


   }

   private Object parseJson(InputStream in) throws JsonProcessingException,
           IOException {
       return JacksonObjectHandler.readJson(in );
   }

   public void addJsonScope(String arg) throws JsonProcessingException,
           IOException {
       getScope().add(parseJson(arg));
   }

   public void addJsonScope(InputStream in) throws JsonProcessingException,
           IOException {
       getScope().add(parseJson(in));

   }

   public void addPropertiesScope(String filename) throws FileNotFoundException,
           IOException {
       Properties p = new Properties();
       try (Reader r = new FileReader(new File(filename))) {
           p.load(r);
       }

       getScope().add(p);
   }

}