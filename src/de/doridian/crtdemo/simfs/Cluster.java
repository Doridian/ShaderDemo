package de.doridian.crtdemo.simfs;

import com.sun.javafx.property.adapter.ReadOnlyJavaBeanPropertyBuilderHelper;

import java.io.*;

public class Cluster {
    public final static short FILE_NAME_SIZE = 8 + 1 + 3;
    public final static short HEADER_SIZE = 1 + 2;

    private final FileSystem fileSystem;
    final int location; //unsigned short

    int attributes; //unsigned byte [Allocated, IsFirst, (Directory, ReadOnly)] Brackets = only first cluster
    int nextCluster; //0 for none

    public byte[] contents;

    private boolean headRead = false, dataRead = false, attributesRead = false;

    //First cluster only
    public String name; //8 bytes + '.' + 3 bytes

    public Cluster(int location, FileSystem fileSystem) {
        this.location = location;
        this.fileSystem = fileSystem;
    }

    public void read(DataInput data) throws IOException {
        if(dataRead)
            return;
        headRead = false;
        readHead(data);
        int dataSize = data.readUnsignedShort();
        contents = new byte[dataSize];
        data.readFully(contents);
        dataRead = true;
    }

    public void readHead(DataInput data) throws IOException {
        if(headRead)
            return;
        attributesRead = false;
        nextCluster = data.readUnsignedShort();
        headRead = true;
    }

    public void readAttributes(DataInput data) throws IOException {
        if(attributesRead)
            return;
        fileSystem.seekToCluster(this);
        attributes = data.readUnsignedByte();
        attributesRead = true;
    }

    public void write(DataOutput data) throws IOException {
        writeAttributes(data);
        data.writeShort(nextCluster);;
        data.writeShort(contents.length);
        data.write(contents, 0, contents.length);
    }

    public void writeAttributes(DataOutput data) throws IOException {
        fileSystem.seekToCluster(this);
        data.writeByte(attributes);
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
