package de.doridian.jsimfs.interfaces;

import java.io.IOException;

public interface IFileSystem {
    public IDirectoryData getRootDirectory();

    public int getClusterSize();
    public int getClusterCount();

    public IAbstractData getFile(String name) throws IOException;
}
