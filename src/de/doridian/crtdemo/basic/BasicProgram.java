package de.doridian.crtdemo.basic;

import de.doridian.crtdemo.Util;
import de.doridian.crtdemo.basic.tokens.AbstractToken;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;

public class BasicProgram {
    private String variableCode = "";
    private String functionCode = "";

    private final boolean debug;

    private float entryPoint = -1;

    private TreeSet<Float> lineNumbers = new TreeSet<>();
    private HashSet<String> definedVariables = new HashSet<>();

    public BasicProgram(boolean debug) {
        this.debug = debug;
    }

    public String getVarType(String varName) {
        int dollarIdx = varName.indexOf('$');

        if(dollarIdx == 0)
            throw new AbstractToken.SyntaxException("VARIABLE NAMES MAY NOT START WITH $");

        if(dollarIdx > 0)
            return "String";
        else
            return "int";

    }

    public void addVariable(String varName, boolean isArray) {
        if(!definedVariables.add(varName))
            return;
        String varType = getVarType(varName);
        if(isArray)
            variableCode += "\tpublic " + varType + "[] " + varName + " = null;\n";
        else
            variableCode += "\tpublic " + varType + " " + varName + ";\n";
    }

    private static int COMPILE_CTR = 0;

    private static final class StringSourceJavaFileObject extends SimpleJavaFileObject {
        final String source;
        final long lastModified;

        StringSourceJavaFileObject(String fullyQualifiedName, String source) {
            super(createUri(fullyQualifiedName), Kind.SOURCE);
            // TODO(gak): check that fullyQualifiedName looks like a fully qualified class name
            this.source = source;
            this.lastModified = System.currentTimeMillis();
        }

        static URI createUri(String fullyQualifiedClassName) {
            return URI.create(fullyQualifiedClassName.replaceAll("\\.", "/") + Kind.SOURCE.extension);
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }

        @Override
        public OutputStream openOutputStream() {
            throw new IllegalStateException();
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(source.getBytes(Charset.defaultCharset()));
        }

        @Override
        public Writer openWriter() {
            throw new IllegalStateException();
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) {
            return new StringReader(source);
        }

        @Override
        public long getLastModified() {
            return lastModified;
        }
    }

    public BaseCompiledProgram compile() {
        String className = "CompileResult$$" + (++COMPILE_CTR);
        String code = getCode(className);
        if(debug)
            Util.writeFile("data/tmp/test.java", code);
        File destDir = new File("data/compiled");
        destDir.mkdirs();
        File classFile = new File(destDir, className + ".class");
        classFile.delete();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        JavaFileObject jfo = new StringSourceJavaFileObject(className, code);
        ArrayList<JavaFileObject> jfoList = new ArrayList<>();
        jfoList.add(jfo);

        Iterable<String> options = Arrays.asList("-d", destDir.getAbsolutePath());
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, options, null, jfoList);

        task.call();

        boolean hasError = false;
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            System.err.println(diagnostic.getLineNumber() + ": " + diagnostic.getMessage(null));
            hasError = true;
        }

        if(hasError)
            return null;

        try {
            URLClassLoader subClassLoader = new URLClassLoader(new URL[] { destDir.toURI().toURL() }, Thread.currentThread().getContextClassLoader());
            BaseCompiledProgram compileResult = (BaseCompiledProgram) subClassLoader.loadClass(className).getConstructor().newInstance();
            compileResult.$sourceCode = code;
            return compileResult;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(!classFile.delete())
                classFile.deleteOnExit();
        }

        return null;
    }

    private void addLineNumber(float line) {
        if(!lineNumbers.add(line))
            throw new AbstractToken.SyntaxException("Line " + Math.floor(line) + " used twice");
        if(lineNumbers.higher(line) != null)
            throw new AbstractToken.SyntaxException("Line " + Math.floor(line) + " appears after higher numbered line");
    }

    public void addNoopLine(float line) {
        addLineNumber(line);
    }

    public void addLine(float line, String code) {
        if(entryPoint < 0)
            entryPoint = line;
        addLineNumber(line);
        functionCode += "\tpublic void $LINE_" + (""+line).replace('.', '_') + "() throws Exception {\n" + code + "\n\t}\n";
    }

    private String getCode(String className) {
        return  "import static de.doridian.crtdemo.basic.BasicFunctions.*;\n\n" +
                "public class " + className + " extends de.doridian.crtdemo.basic.BaseCompiledProgram { \n" +
                "\tpublic " + className + "() {\n\t\tsuper(" + className + ".class, " + entryPoint + "f);\n\t}\n\n" +
                variableCode + "\n" +
                functionCode +
                "}";
    }
}
