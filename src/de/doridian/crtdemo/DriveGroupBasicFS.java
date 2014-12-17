package de.doridian.crtdemo;

import de.doridian.jbasic.BasicFS;
import de.doridian.jsimfs.interfaces.IFileData;

import java.io.IOException;

public class DriveGroupBasicFS implements BasicFS {
    private final DriveGroup fs;

    public DriveGroupBasicFS(DriveGroup fs) {
        this.fs = fs;
    }

    @Override
    public String getFileContents(String fileName) throws IOException {
        IFileData file = (IFileData)fs.getFile(fileName);
        return new String(file.readFully(), "ASCII");
    }

    @Override
    public BasicFSFile openFile(String fileName) throws IOException {
        IFileData file = (IFileData)fs.getFile(fileName);
        return new SimFSBasicFS.SimFSBasicFSFile(file);
    }
}
