package de.doridian.crtdemo.basic;

import de.doridian.crtdemo.Util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RealFSBasicFS implements BasicFS {
    @Override
    public String getFileContents(String fileName) throws IOException {
        return Util.readFile("data/fssrc/C/" + fileName);
    }

    @Override
    public BasicFSFile openFile(String fileName) throws IOException {
        return new RealFSBasicFSFile(new RandomAccessFile("data/fssrc/C/" + fileName, "rw"));
    }

    public static class RealFSBasicFSFile implements BasicFSFile {
        private final RandomAccessFile ranAF;

        public RealFSBasicFSFile(RandomAccessFile ranAF) {
            this.ranAF = ranAF;
        }

        @Override
        public void setLength(int len) throws IOException {
            ranAF.setLength(len);
        }

        @Override
        public String readLine() throws IOException {
            return ranAF.readLine();
        }

        @Override
        public void writeLine(String line) throws IOException {
            ranAF.writeBytes(line + "\n");
        }

        @Override
        public void close() throws IOException {
            ranAF.close();
        }
    }
}
