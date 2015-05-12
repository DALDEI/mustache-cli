package org.xmlsh.tools.mustache.cli.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.tools.mustache.cli.functions.FileFunctions;
import org.xmlsh.tools.mustache.cli.functions.JsonFunctions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheResolver;

public class MustacheContext {

  static Logger mLogger = LogManager.getLogger();

  private File mCurDir;

  public MustacheContext() {
    this(new File(System.getProperty(".")));
  }

  @SuppressWarnings("serial")
  public MustacheContext(File curdir) {
    final MustacheContext c = this;
    mCurDir = curdir;
    scope.add(new HashMap<String, Object>() {
      {
        put("json", JsonFunctions.jsonFunction(c));
        put("array", JsonFunctions.arrayFunction(c));
        put("quote", JsonFunctions.quoteFunction(c));
        put("include", FileFunctions.includeFunction(c));
        // put("lines",FileFunctions.includeFunction(c));
        put("reparse", FileFunctions.reparseFunction(c));

      }
    });

  }

  // Filesystem based resolver that is encoding configurable
  public class EncodingAwareResolver implements MustacheResolver {

    /*
     * For simple File-Not-Found this needs to return null
     * 
     * @see com.github.mustachejava.MustacheResolver#getReader(java.lang.String)
     */
    @Override
    public Reader getReader(String resourceName) {
      mLogger.entry(resourceName);
      mLogger.info("Resolving reader for file: {}", resourceName);
      try {
        return mLogger.exit(getFileReader(resourceName));
      } catch (FileNotFoundException | UnsupportedEncodingException e) {
        throw mLogger.throwing(new MustacheException("Error reading resource: "
            + resourceName, e));
      }
    }

  }

  private final class JacksonMustacheFactory extends DefaultMustacheFactory {
    private JacksonMustacheFactory(MustacheResolver mustacheResolver) {
      super(mustacheResolver);
      setObjectHandler(new JacksonObjectHandler());

    }

    @Override
    public void encode(String value, Writer writer) {
      try {
        if (encoder == null)
          writer.write(value);
        else
          encoder.accept(value, writer);
      } catch (final Exception e) {
        addError(e);
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
  private BiConsumer<String, Writer> encoder;
  private final List<File> mTemplatePath = new ArrayList<>();
  private Exception mErrors = null;
  private String mInputEncoding = System.getProperty("file.encoding");
  private String mOutpuEncoding = System.getProperty("file.encoding");

  public String getOutpuEncoding() {
    return mOutpuEncoding;
  }

  public void setOutpuEncoding(String outpuEncoding) {
    mOutpuEncoding = outpuEncoding;
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

  public void setEncoder(BiConsumer<String, Writer> encoder) {
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
    if (mFactory == null) {
      mFactory = new JacksonMustacheFactory(getResolver());
    }
    return mFactory;
  }

  private MustacheResolver getResolver() {
    return new EncodingAwareResolver();
  }

  protected void addError(Exception e) {
    if (mErrors == null)
      mErrors = e;
    else
      mErrors.addSuppressed(e);

  }

  public void execute() throws Exception {
    mLogger.entry();
    assert (output != null);
    assert (getTemplate() != null);

    final Mustache mustache = getMustacheFactory().compile(getTemplate(),
        getTemplate_name() == null ? "main" : getTemplate_name(),
        getDelimStart(), getDelimEnd());

    mustache.execute(getOutput(), getScope().toArray());
    if (mErrors != null)
      throw mErrors;
  }

  public Object parseJsonToMap(Reader r) {
    try {
      return JacksonObjectHandler.readJsonAsMap(r);
    } catch (final IOException e) {
      throw new MustacheException(e);
    }

  }

  public Object convertJson(JsonNode r) {
    return JacksonObjectHandler.convertJson(r);

  }

  private Object parseJsonToMap(String s) {

    return JacksonObjectHandler.readJsonAsMap(s);
  }

  private Object parseJsonToMap(InputStream in) {
    return JacksonObjectHandler.readJsonAsMap(in);
  }

  public void addJsonScope(String arg) {
    getScope().add(parseJsonToMap(arg));
  }

  public void addJsonScope(InputStream in) {
    getScope().add(parseJsonToMap(in));

  }

  public void addPropertiesScope(String filename) throws FileNotFoundException,
      IOException {
    try (Reader r = getFileReader(filename)) {
      addPropertiesScope(r);
    }
  }

  public Reader getFileReader(String filename) throws FileNotFoundException,
      UnsupportedEncodingException {
    mLogger.entry(filename);
    final File file = resolveTemplateFile(filename);
    if (file == null)
      return mLogger.exit(null);
    mLogger.info("resolved to file {}", file.getAbsolutePath());
    return getStreamReader(new FileInputStream(file));

  }

  public Reader getStreamReader(InputStream in) throws FileNotFoundException,
      UnsupportedEncodingException {
    return new InputStreamReader(in, getInputEncoding());
  }

  public String getInputEncoding() {
    return mInputEncoding;
  }

  File newFile(String filename) {
    return new File(mCurDir, filename);
  }

  File resolveTemplateFile(String filename) {
    mLogger.entry(filename);
    File file = tryFile(newFile(filename));
    if (file != null)
      return file;
    for (final File root : mTemplatePath) {
      file = tryFile(new File(root, filename));
      if (file != null)
        return mLogger.exit(file);

    }
    return mLogger.exit(null);
  }

  File tryFile(File file) {
    mLogger.entry(file);
    if (!file.exists())
      return null;

    if (!file.isFile() || !file.canRead())
      throw new MustacheException("File does not exist or is unreadable: "
          + file);

    return mLogger.exit(file);
  }

  public File resolveFile(String filename) {
    return resolveTemplateFile(filename);
  }

  public void addTemplateRoot(File file) {
    assert (file != null);
    assert (file.isDirectory());
    if (!file.exists()) {
      throw new MustacheException(file + " does not exist");
    }
    if (!file.isDirectory()) {
      throw new MustacheException(file + " is not a directory");
    }
    mTemplatePath.add(file);

  }

  public void addPropertiesScope(Reader reader) throws IOException {
    final Properties p = new Properties();
    p.load(reader);
    getScope().add(p);
  }

  public void addJsonScope(Reader reader) {

    getScope().add(parseJsonToMap(reader));
  }

  public void addJsonScope(JsonNode json) throws JsonProcessingException,
      IOException {
    getScope().add(convertJson(json));

  }

  public void addStringScope(String string) {
    final String[] pair = string.split("=");
    if (pair == null || pair.length != 2)
      throw new MustacheException("Unparsable context string: " + string);
    getScope().add(Collections.singletonMap(pair[0], pair[1]));

  }

  public void addObjectScope(Object obj) {
    getScope().add(obj);
  }

  void setInputEncoding(String encoding) {
    mInputEncoding = encoding;
  }

  public void setOutput(OutputStream out) throws UnsupportedEncodingException {
    setOutput(new OutputStreamWriter(out, getOutpuEncoding()));

  }

  public File getCurDir() {
    return mCurDir;
  }

  public void setCurDir(File mCurDir) {
    this.mCurDir = mCurDir;
  }

}
