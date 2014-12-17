package de.doridian.jsimfs.interfaces;

import java.io.IOException;

public interface IFileSystem {
    public IDirectoryData getRootDirectory();

    public int getClusterSize();
    public int getClusterCount();

    public void setCWD(IDirectoryData cwd) throws IOException;
    public IDirectoryData getCWD() throws IOException;

    public IAbstractData getFile(String name) throws IOException;
}
