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
        return new String(((IFileData)fs.getFile(fileName)).readFully(), "ASCII");
    }
}
