package de.doridian.crtdemo.simfs.interfaces;

import java.io.IOException;

public interface IDirectoryData extends IAbstractData {
    public IAbstractData findFile(String fileName) throws IOException;

    public IFileData createFile(String name) throws IOException;
    public IDirectoryData createDirectory(String name) throws IOException;

    public IAbstractData[] listFiles() throws IOException;
}
