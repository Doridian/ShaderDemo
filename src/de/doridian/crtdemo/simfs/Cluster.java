package de.doridian.crtdemo.simfs;

import java.io.*;

public class Cluster {
    public final static short HEADER_SIZE = 1 + 2 + 1;

    final FileSystem fileSystem;
    public final int location; //unsigned short

    int attributes; //unsigned byte [Allocated, IsFirst, (Directory, ReadOnly)] Brackets = only first cluster
    int nextCluster; //0 for none

    private boolean headRead = false, attributesRead = false;

    public Cluster(int location, FileSystem fileSystem) {
        this.location = location;
        this.fileSystem = fileSystem;
    }

    public byte[] read() throws IOException {
        return read(0, Integer.MAX_VALUE);
    }

    public byte[] read(int offset, int maxLen) throws IOException {
        if(!readHead())
            fileSystem.randomAccessFile.skipBytes(2);
        int dataSize = fileSystem.randomAccessFile.readUnsignedByte();
        if(offset > 0)
            dataSize -= offset;
        if(dataSize <= 0)
            return new byte[0];
        if(dataSize > maxLen)
            dataSize = maxLen;
        byte[] contents = new byte[dataSize];
        fileSystem.randomAccessFile.skipBytes(offset);
        fileSystem.randomAccessFile.readFully(contents);
        return contents;
    }

    public boolean readHead() throws IOException {
        if(!readAttributes())
            fileSystem.randomAccessFile.skipBytes(1);
        if(headRead)
            return false;
        nextCluster = fileSystem.randomAccessFile.readUnsignedShort();
        headRead = true;
        return true;
    }

    public boolean readAttributes() throws IOException {
        fileSystem.seekToCluster(this);
        if(attributesRead)
            return false;
        attributes = fileSystem.randomAccessFile.readUnsignedByte();
        attributesRead = true;
        return true;
    }

    public void write(byte[] contents) throws IOException {
        writeHead();
        fileSystem.randomAccessFile.writeByte(contents.length);
        fileSystem.randomAccessFile.write(contents, 0, contents.length);
    }

    public void writeAttributes() throws IOException {
        fileSystem.seekToCluster(this);
        fileSystem.randomAccessFile.writeByte(attributes);
        attributesRead = true;
    }

    public void writeHead() throws IOException {
        writeAttributes();
        fileSystem.randomAccessFile.writeShort(nextCluster);
        headRead = true;
    }

    public boolean hasAllAttributes(int attribute) {
        return (attributes & attribute) == attribute;
    }

    public void setAttribute(int attribute, boolean set) {
        if(set)
            attributes |= attribute;
        else
            attributes &= ~attribute;
    }

    public static final byte ATTRIBUTE_ALLOCATED = 1;
    public static final byte ATTRIBUTE_FIRST = 2;
    public static final byte ATTRIBUTE_DIRECTORY = 4;
    public static final byte ATTRIBUTE_READONLY = 8;
}
