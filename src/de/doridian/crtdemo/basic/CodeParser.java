package de.doridian.crtdemo.basic;

import de.doridian.crtdemo.Util;
import de.doridian.crtdemo.basic.tokens.AbstractToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CodeParser {
    private final String[] lines;
    private final boolean debug;

    public CodeParser(String code, boolean debug) {
        lines = code.split("[\r\n]+");
        this.debug = debug;
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

    public static void main(String[] args) {
        final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        BaseCompiledProgram compiledProgram = new CodeParser(Util.readFile("data/tmp/test.basic"), true).compile();

        compiledProgram.$start(new BasicIO() {
            @Override
            public String getLine() {
                try {
                    return inputReader.readLine().trim();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void print(Object obj) {
                System.out.print(obj);
                System.out.flush();
            }

            @Override
            public void setCursor(int x, int y) {

            }
        });
    }
}
