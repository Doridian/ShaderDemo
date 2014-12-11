package de.doridian.crtdemo.basic;
import de.doridian.crtdemo.basic.tokens.AbstractToken;

import java.io.IOException;

public class CodeParser {
    private final String[] lines;
    private final boolean debug;
    private final BasicFS fs;

    private CodeParser(String code, BasicFS fs, boolean debug) throws IOException {
        this.fs = fs;
        this.lines = code.split("[\r\n]+");
        this.debug = debug;
    }

    public CodeParser(BasicFS fs, String fileName, boolean debug) throws IOException {
        this(fs.getFileContents(fileName), fs, debug);
    }

    public BaseCompiledProgram compile() {
        BasicProgram program = new BasicProgram(debug);
        for(int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if(line.charAt(0) == '#' || line.isEmpty())
                continue;
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
        compiledProgram.$fs = fs;
        compiledProgram.$debug = debug;
        return compiledProgram;
    }
}
