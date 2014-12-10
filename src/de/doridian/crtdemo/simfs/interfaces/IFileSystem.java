package de.doridian.crtdemo.simfs.interfaces;

public interface IFileSystem {
    public IDirectoryData getRootDirectory();

    public int getClusterSize();
    public int getClusterCount();
}
