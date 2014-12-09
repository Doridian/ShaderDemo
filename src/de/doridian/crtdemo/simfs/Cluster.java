package de.doridian.crtdemo.simfs;

import com.sun.javafx.property.adapter.ReadOnlyJavaBeanPropertyBuilderHelper;

import java.io.*;

public class Cluster {
    public final static short FILE_NAME_SIZE = 8 + 1 + 3;
    public final static short HEADER_SIZE = 1 + 2;

    final FileSystem fileSystem;
    final int location; //unsigned short

    int attributes; //unsigned byte [Allocated, IsFirst, (Directory, ReadOnly)] Brackets = only first cluster
    int nextCluster; //0 for none

    private boolean headRead = false, attributesRead = false;

    //First cluster only
    public String name; //8 bytes + '.' + 3 bytes

    public Cluster(int location, FileSystem fileSystem) {
        this.location = location;
        this.fileSystem = fileSystem;
    }

    public byte[] read() throws IOException {
        headRead = false;
        readHead();
        int dataSize = fileSystem.randomAccessFile.readUnsignedShort();
        byte[] contents = new byte[dataSize];
        fileSystem.randomAccessFile.readFully(contents);
        return contents;
    }

    public void readHead() throws IOException {
        if(headRead)
            return;
        attributesRead = false;
        readAttributes();
        nextCluster = fileSystem.randomAccessFile.readUnsignedShort();
        headRead = true;
    }

    public void readAttributes() throws IOException {
        if(attributesRead)
            return;
        fileSystem.seekToCluster(this);
        attributes = fileSystem.randomAccessFile.readUnsignedByte();
        attributesRead = true;
    }

    public void write(byte[] contents) throws IOException {
        writeHead();
        fileSystem.randomAccessFile.writeShort(contents.length);
        fileSystem.randomAccessFile.write(contents, 0, contents.length);
    }

    public void writeAttributes() throws IOException {
        fileSystem.seekToCluster(this);
        fileSystem.randomAccessFile.writeByte(attributes);
    }

    public void writeHead() throws IOException {
        writeAttributes();
        fileSystem.randomAccessFile.writeShort(nextCluster);;
    }

    public boolean isAttribute(int attribute) {
        return (attributes & attribute) == attribute;
    }

    public void setAttribute(int attribute, boolean set) {
        if(set)
            attributes |= attribute;
        else
            attributes &= ~attribute;
    }

    public static final byte ATTRIBUTE_ALLOCATED = 0x01;
    public static final byte ATTRIBUTE_FIRST = 0x02;
    public static final byte ATTRIBUTE_DIRECTORY = 0x04;
    public static final byte ATTRIBUTE_READONLY = 0x08;
}
