package de.doridian.crtdemo.simfs.data;

import de.doridian.crtdemo.simfs.AbstractData;
import de.doridian.crtdemo.simfs.FileSystem;
import de.doridian.crtdemo.simfs.interfaces.IFileData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FileData extends AbstractData implements IFileData {
    public FileData(FileSystem fileSystem) {
        super(fileSystem);
    }

    @Override
    public void setAutoFlush(boolean autoFlush) {
        super.setAutoFlush(autoFlush);
        this.directAutoFlush = autoFlush;
    }

    @Override
    public byte[] readFully() throws IOException {
        seek(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int readLen = 0;
        while((readLen = read(buffer, 0, buffer.length)) >= 0) {
            baos.write(buffer, 0, readLen);
        }
        return baos.toByteArray();
    }
}
