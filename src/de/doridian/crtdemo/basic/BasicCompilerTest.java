package de.doridian.crtdemo.basic;

import de.doridian.crtdemo.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BasicCompilerTest {
    public static void main(String[] args) throws Exception {
        final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        BaseCompiledProgram compiledProgram = new CodeParser(Util.readFile("data/test.basic"), true).compile();

        compiledProgram.$start(new BasicIO() {
            @Override
            public String readLine() {
                try {
                    return inputReader.readLine().trim();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void clearLine(int line) {

            }

            @Override
            public void clearScreen() {

            }

            @Override
            public int getLines() {
                return 9999;
            }

            @Override
            public int getColumns() {
                return 256;
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