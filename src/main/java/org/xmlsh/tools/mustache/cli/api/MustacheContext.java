package org.xmlsh.tools.mustache.cli.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.function.BiConsumer;
















import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.mustachejava.DefaultMustacheFactory; 
import com.github.mustachejava.resolver.DefaultResolver;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheResolver;

public class MustacheContext {
    
    
    
    
    // Filesystem based resolver that is encoding configurable
    public class EncodingAwareResolver implements MustacheResolver {

    	/* 
    	 * For simple File-Not-Found this needs to return null
    	 * @see com.github.mustachejava.MustacheResolver#getReader(java.lang.String)
    	 */
        @Override
        public Reader getReader(String resourceName) {
                try {
					return getFileReader( resourceName );
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					throw new MustacheException( "Error reading resource: " + resourceName , e );
				}
        }

    }

    private final class JacksonMustacheFactory extends DefaultMustacheFactory {
        private JacksonMustacheFactory(MustacheResolver mustacheResolver) {
            super(mustacheResolver);
           setObjectHandler(new JacksonObjectHandler());

        }

        @Override
        public void encode(String value, Writer writer)  { 
            try {
               if( encoder == null )
                   writer.write(value);
               else
                 encoder.accept(value, writer);
            } catch( Exception e ){
                addError( e );
            }
        }
    }

    private DefaultMustacheFactory mFactory;
    private ArrayList<Object> scope = new ArrayList<>();
    private Reader template;
    private Writer output;
    private String template_name;
    private String delimStart = "{{";
    private String delimEnd = "}}";
    private BiConsumer<String, Writer> encoder ;
    private File mRoot;
    private Exception mErrors = null ;
    private String mInputEncoding = System.getProperty("file.encoding");
    private String mOutpuEncoding = System.getProperty("file.encoding");

    public String getOutpuEncoding() {
        return mOutpuEncoding;
    }

    public void setOutpuEncoding(String outpuEncoding) {
        mOutpuEncoding = outpuEncoding;
    }

    public File getRoot() {
        return mRoot == null ? new File( ".") : mRoot ; 
    }

    public String getDelimEnd() {
        return delimEnd;
    }

    public String getDelimStart() {
        return delimStart;
    }

    public BiConsumer<String, Writer> getEncoder() {
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

    public void setEncoder(  BiConsumer<String, Writer> encoder) {
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
        if (mFactory == null){
            mFactory = new JacksonMustacheFactory( getResolver() );
        }
        return mFactory;
    }

    private MustacheResolver getResolver() {
       return new EncodingAwareResolver();
    }

    protected void addError(Exception e) {
        if( mErrors == null )
            mErrors  = e ; 
        else
            mErrors.addSuppressed(e);
        
    }

    public void execute() throws Exception {
            assert( output != null);
            assert( getTemplate() != null);


            Mustache mustache = getMustacheFactory().compile(getTemplate(),
                    getTemplate_name() == null ? "main" : getTemplate_name(),
                            getDelimStart(), getDelimEnd());

            mustache.execute(getOutput(), getScope().toArray());
            if( mErrors != null )
                throw mErrors ;
    }


    public Object parseJson(Reader r) throws JsonProcessingException,
    IOException {
        return JacksonObjectHandler.readJson(r);

    }

    public Object convertJson(JsonNode r) throws JsonProcessingException,
    IOException {
        return JacksonObjectHandler.convertJson(r);

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
        try (Reader r = getFileReader(filename)) {
            addPropertiesScope(r);
        }
    }

     public Reader getFileReader(String filename) throws FileNotFoundException, UnsupportedEncodingException { 
        File file =  resolveTemplateFile(filename);
        if( file == null )
        	return null;
        return getStreamReader(
                new FileInputStream( file) );
    }
     public Reader getStreamReader(InputStream in )
             throws FileNotFoundException, UnsupportedEncodingException {
         return new InputStreamReader(in, getInputEncoding() );
     }

     String getInputEncoding() {
         return mInputEncoding ;
    }

    File resolveTemplateFile( String filename ){

         File file;
         if( getRoot() == null )
             file =  new File( filename );
         else
             file = new File( getRoot(), filename );
         if( !file.exists())
        	 return null; 
         if( ! file.isFile() || ! file.canRead() )
                 throw new MustacheException("File does not exist or is unreadable: " + file);

         return file;
    
    
    }

    public void setRoot(File file) {
        assert( file != null );
        assert( file.isDirectory());
        if (!file.exists()) {
            throw new MustacheException(file + " does not exist");
          }
          if (!file.isDirectory()) {
            throw new MustacheException(file + " is not a directory");
          }
          this.mRoot = file;

    }

    public void addPropertiesScope(Reader reader) throws IOException {
        Properties p = new Properties();
        p.load(reader);
        getScope().add(p);    
    }

    public void addJsonScope(Reader reader) throws JsonProcessingException, IOException {

        getScope().add(parseJson(reader));
    }

    public void addJsonScope(JsonNode json) throws JsonProcessingException, IOException {
        getScope().add(convertJson(json));

    }

    public void addStringScope(String string) {
        String[] pair = string.split("=");
        if( pair == null || pair.length != 2 )
             throw new MustacheException("Unparsable context string: " + string);
        getScope().add(Collections.singletonMap(pair[0], pair[1]));

    }

    public void addObjectScope( Object obj ){
        getScope().add(obj);
    }

    void setInputEncoding(String encoding) {
        mInputEncoding = encoding;
    }

    public void setOutput(OutputStream out) throws UnsupportedEncodingException {
     setOutput( new OutputStreamWriter( out , getOutpuEncoding()));
        
    }

}