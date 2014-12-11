package de.doridian.crtdemo.basic;
import de.doridian.crtdemo.basic.tokens.AbstractToken;
import de.doridian.crtdemo.simfs.interfaces.IFileData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CodeParser {
    private final String[] lines;
    private final boolean debug;
    private final BasicFS fs;

    public CodeParser(BasicFS fs, String code, boolean debug) throws IOException {
        this.fs = fs;
        this.lines = code.split("[\r\n]+");
        this.debug = debug;
    }

    public CodeParser(String code, boolean debug) throws IOException {
        this(new RealFSBasicFS(), code, debug);
    }

    public CodeParser(IFileData code, boolean debug) throws IOException {
        this(new SimFSBasicFS(code.getFileSystem()), new String(code.readFully(), "ASCII"), debug);
    }

    public CodeParser(InputStream code, boolean debug) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(code));

        ArrayList<String> lineList = new ArrayList<>();
        String line;
        while((line = reader.readLine()) != null)
            lineList.add(line.trim());

        this.lines = lineList.toArray(new String[lineList.size()]);
        this.debug = debug;
        this.fs = new RealFSBasicFS();
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
        return program.compile();
    }
}
