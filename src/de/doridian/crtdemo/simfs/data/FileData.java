package de.doridian.crtdemo.simfs.data;

import de.doridian.crtdemo.simfs.AbstractData;
import de.doridian.crtdemo.simfs.FileSystem;
import de.doridian.crtdemo.simfs.interfaces.IFileData;

public class FileData extends AbstractData implements IFileData {
    public FileData(FileSystem fileSystem) {
        super(fileSystem);
    }
}
