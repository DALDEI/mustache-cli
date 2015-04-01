package org.xmlsh.tools.mustache.cli.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.xmlsh.tools.mustache.cli.api.JacksonObjectHandler;
import org.xmlsh.tools.mustache.cli.api.MustacheContext;
import org.xmlsh.tools.mustache.cli.main.Main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.util.HtmlEscaper;

public class Main {
  
    static class Opt { 
        List<String>   opts;
        String description;
        Opt( String... opts ){
            this.opts = Arrays.asList(opts);
        }
        Opt with( String desc ){
            description = desc ;
            return this ;
        }
        static Opt option( String... opts ) {
            return new Opt(opts);
        } 
    }


    static List<Opt>  opts = Arrays.asList(
          Opt.option ( "--root" , "-R" ,"--template-dir" ).with("Template directory root"),
          Opt.option ( "-f", "--template-file").with("Template file (or '-') "),
          Opt.option ( "-t", "--template","--template-data").with("Template data (inline) "),
          Opt.option ( "-p", "--properties-file").with("Context read from Java properties file" ),
          Opt.option ( "-j", "--json-data").with("Context  (inline) as JSON" ),
          Opt.option ( "-J", "--json-file").with("Context  read from JSON file" ),
          Opt.option ( "-o", "--output","--output-file").with("Write to output file (or '-')" ),
          Opt.option ( "-n", "--name").with("Template name"),
          Opt.option ( "-S", "--delim-start").with("Delmitar start string [default '{{' ]"),
          Opt.option ( "-E", "--delim-end").with("Delmitar end string [default '}}' ]"),
          Opt.option ( "--json").with("Use JSON encoded data for variable expansion"),
          Opt.option ( "--html").with("Use HTML encoded data for variable expansion"),
          Opt.option ( "-h","--help").with("Help")
      );
          
          

    
    
    public static class Usage {
        public List<String> message;
        public String header = "Usage: mustache [options] [template] [context]";
        
        Usage( String... msg ){
            message = Arrays.asList(msg);
        }

        
        public void getOptions( Consumer<String> out ) {
           for( Opt o : opts  ){
               out.accept( o.opts.stream().collect(Collectors.joining(" ")).concat("\t").concat(o.description) );
           }
         }
    }
        @SuppressWarnings("serial")
        
        public  static class  UsageException extends Exception {
             Usage usage;

            public Usage getUsage() { return usage ; } 
            public void write(PrintStream err) {
                usage.message.forEach( err::println );
                err.println(usage.header);
                usage.getOptions( err::println  );
            }
        }
        

         @SuppressWarnings("serial")
        static void usage(final String... msg) throws UsageException {
                throw new UsageException() {{
                    usage = new Usage(msg);
                }};
        }
                           
        
        
	
	
    private MustacheContext mContext = new MustacheContext();


	
	private String requiresArg(int i, String args[]) throws UsageException {
		if (i >= args.length)
			usage("Expecting argument to " + args[i - 1]);
		return args[i];
	}
	
	
	public Main(String[] args
	     ) throws IOException, UsageException {
	    
	    
		for (int i = 0;  i < args.length; i++){
			 if( args[i].startsWith("-") ){
				switch (args[i]) {
				
				case "--root" : 
				case "-R" :
				case "--template-dir" : 
					mContext.setRoot(  new File(requiresArg(++i,args)));
				break;
				
				case "-f":
				case "--template-file": {

					String fname = requiresArg(++i, args);
					if (fname.equals("-"))
						mContext.setTemplate(new InputStreamReader(System.in)) ;
					else {
						mContext.setTemplate(mContext.getMustacheFactory().getReader(fname));
						if( mContext.getTemplate_name() != null )
						  mContext.setTemplate_name(fname) ;
					}
					break;
				}
				case "-t":
				case "--template":
					String a = requiresArg(++i, args);
					mContext.setTemplate(new StringReader(a));

					break;
				case "-p":
				case "--properties-file":
					mContext.addPropertiesScope(requiresArg(++i, args));
					break;
				case "-j":      
				case "--json-data":
				    mContext.addJsonScope(requiresArg(++i, args));
					break;
				case "-J":
				case "--json-file": {
					String fname = requiresArg(++i, args);
					if (fname.equals("-"))
					    mContext.addJsonScope( mContext.getStreamReader(System.in ));
                    else
                        try (Reader r = mContext.getFileReader( fname ) ){
                        	Object jn = mContext.parseJson(r);
                        	mContext.getScope().add(jn);
                        }
					break;
				}
				case "-n":
				case "--name":
					mContext.setTemplate_name(args[i]);
					break;
				case "-o":
				case "--output": {
					String fname = requiresArg(++i, args);
					if (fname.equals("-"))
						mContext.setOutput( System.out);
					else
						mContext.setOutput( new FileOutputStream(new File(fname)));
					break;
				}
				
				case "-S" :
				case "-delim-start" :
					mContext.setDelimStart(requiresArg(++i, args)); 
					break;
				case "-E" :
				case "-delim-end" :
					mContext.setDelimEnd(requiresArg(++i, args)); 
					break ;
				case "--json" :
				  break;
				case "--html" :
				   mContext.setEncoder((v,w) -> HtmlEscaper.escape(v,w,true));
		           break;
				case "-h" : case "--help"  :
				   usage();
				   break;
				default:
					usage("Unknown option:" + args[i]);
				}
		} else {

		   if (mContext.getTemplate() == null)
				mContext.setTemplate(new StringReader(args[i]));
	     else       
			  addScopePair( args[i] );
		}
	}
	if (mContext.getTemplate() == null) 
		mContext.setTemplate(new InputStreamReader( System.in));
	   if (mContext.getOutput() == null)
			mContext.setOutput(new PrintWriter(System.out));
	}


    public void addScopePair(String string) {
		mContext.addStringScope( string );
		
	}



	public static void main(String[] args) throws IOException {

		try {
		    new Main(args).run();
		}catch( UsageException e ){

		    e.write( System.err );
		    
		    System.exit( 1 );
		}
		catch ( Throwable e ){
		    e.printStackTrace(System.err);
		}

	}
	

	public void run() throws Exception {
		try {
		    mContext.execute();
		} finally {
			mContext.getTemplate().close();
			mContext.getOutput().close();
		}
	}


}
