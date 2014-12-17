package de.doridian.jbasic;

import java.io.IOException;

public interface BasicFS {
    public String getFileContents(String fileName) throws IOException;

    public BasicFSFile openFile(String fileName) throws IOException;

    public interface BasicFSFile {
        public void setLength(int len) throws IOException;

        public String readLine() throws IOException;
        public void writeLine(String line) throws IOException;

        public void close() throws IOException;
    }
}
