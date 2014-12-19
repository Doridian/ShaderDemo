package de.doridian.jbasic;
import de.doridian.jbasic.tokens.AbstractToken;

import java.io.IOException;
import java.util.ArrayList;

public class CodeParser {
    private final String[] lines;
    private final boolean debug;
    private final BasicFS fs;

    public CodeParser(BasicFS fs, String fileName) throws IOException {
        this(fs, fileName, System.getProperty("jbasic.debug").equalsIgnoreCase("true"));
    }

    public CodeParser(BasicFS fs, String fileName, boolean debug) throws IOException {
        this.fs = fs;
        this.debug = debug;

        ArrayList<String> codeLines = addPreprocessedFile(fileName);
        this.lines = codeLines.toArray(new String[codeLines.size()]);
    }

    private ArrayList<String> addPreprocessedFile(String fileName) throws IOException {
        ArrayList<String> codeLines = new ArrayList<>();

        for(String line : fs.getFileContents(fileName).split("[\r\n]+")) {
            line = line.trim();
            if(line.isEmpty() || line.charAt(0) == '\'')
                continue;

            if(line.charAt(0) != '#') {
                codeLines.add(line);
                continue;
            }

            String[] preprocessorArgs = line.substring(1).split(" +");
            switch (preprocessorArgs[0].toLowerCase()) {
                case "include":
                    codeLines.addAll(addPreprocessedFile(preprocessorArgs[1]));
                    break;
                default:
                    throw new RuntimeException("Invalid preprocessor directive");
            }
        }

        return codeLines;
    }

    public BaseCompiledProgram compile() {
        BasicProgram program = new BasicProgram(debug);
        for(int i = 0; i < lines.length; i++) {
            String line = lines[i];
            AbstractToken token = AbstractToken.parseLine(program, line);
            Class<? extends AbstractToken> endingToken = token.getEndingToken();
            if(endingToken != null) {
                int nestings = 0;
                boolean tokenFound = false;
                for(int j = i + 1; j < lines.length; j++) {
                    String innerLine = lines[j];
                    AbstractToken innerToken = AbstractToken.parseLine(program, innerLine);
                    if(endingToken.isAssignableFrom(innerToken.getClass())) {
                        if(nestings-- == 0) {
                            token.setEndingToken(innerToken);
                            tokenFound = true;
                            break;
                        }
                    } else if(endingToken.isAssignableFrom(token.getClass())) {
                        nestings++;
                    }
                }
                if(!tokenFound)
                    throw new AbstractToken.SyntaxException("UNEXPECTED EOF");
            }
            token.insert();
        }

        BaseCompiledProgram compiledProgram = program.compile();
        if(compiledProgram == null)
            throw new AbstractToken.SyntaxException("COMPILE ERROR");

        compiledProgram.$fs = fs;
        compiledProgram.$debug = debug;
        
        return compiledProgram;
    }
}
