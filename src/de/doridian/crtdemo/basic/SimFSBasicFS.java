package de.doridian.crtdemo.basic;

import de.doridian.crtdemo.simfs.interfaces.IFileData;
import de.doridian.crtdemo.simfs.interfaces.IFileSystem;

import java.io.IOException;

public class SimFSBasicFS implements BasicFS {
    private final IFileSystem fs;

    public SimFSBasicFS(IFileSystem fs) {
        this.fs = fs;
    }

    @Override
    public String getFileContents(String fileName) throws IOException {
        IFileData file = (IFileData)fs.getFile(fileName);
        return new String(file.readFully(), "ASCII");
    }

    @Override
    public BasicFSFile openFile(String fileName) throws IOException {
        return new SimFSBasicFSFile((IFileData)fs.getFile(fileName));
    }

    public static class SimFSBasicFSFile implements BasicFSFile {
        private final IFileData fsFile;

        public SimFSBasicFSFile(IFileData fsFile) {
            this.fsFile = fsFile;
        }

        @Override
        public void setLength(int len) throws IOException {
            fsFile.setLength(len);
        }

        @Override
        public String readLine() throws IOException {
            return fsFile.readLine();
        }

        @Override
        public void writeLine(String line) throws IOException {
            fsFile.writeBytes(line + "\n");
        }

        @Override
        public void close() throws IOException {
            fsFile.close();
        }
    }
}
