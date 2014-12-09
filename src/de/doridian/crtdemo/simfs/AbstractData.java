package de.doridian.crtdemo.simfs;

import java.io.Closeable;
import java.io.IOException;
import java.util.TreeMap;

public class AbstractData extends SimpleDataInputOutput implements Closeable {
    public Cluster attributeCluster;
    protected FileSystem fileSystem;
    String name;

    protected TreeMap<Integer, byte[]> dataClustersDirty = new TreeMap<>();
    protected boolean attributesDirty;

    private int currentPos = 0;

    protected int getSetAttributes() {
        return 0;
    }

    private int getClusterFor(int pos) {
        return pos / getClusterDataSize();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.attributesDirty = true;
    }

    private int getClusterDataSize() {
        return fileSystem.clusterSize - Cluster.HEADER_SIZE;
    }

    protected void writeAbsolute(int filePos, byte[] data, int pos, int len) throws IOException {
        int startCluster = getClusterFor(filePos);
        int endCluster = getClusterFor(filePos + len - 1);
        int writtenData = 0;
        for(int cluster = startCluster; cluster <= endCluster; cluster++) {
            int curLen = Math.min(getClusterDataSize(), len - writtenData);
            byte[] clusterData = new byte[curLen];
            System.arraycopy(data, pos + writtenData, clusterData, 0, curLen);
            writtenData += curLen;
            dataClustersDirty.put(cluster, clusterData);
        }
    }

    protected int readAbsolute(int filePos, byte[] data, int pos, int len) throws IOException {
        if(attributeCluster == null)
            return 0;
        int startCluster = getClusterFor(filePos);
        int endCluster = getClusterFor(filePos + len);
        int readLen = 0;
        Cluster currentCluster = attributeCluster;
        for(int cluster = 0; cluster <= endCluster; cluster++) {
            currentCluster.readHead();
            if(currentCluster.nextCluster <= 0)
                break;
            currentCluster = fileSystem.getCluster(currentCluster.nextCluster);
            if(cluster < startCluster)
                continue;
            byte[] dataRead;
            if(dataClustersDirty.containsKey(cluster))
                dataRead = dataClustersDirty.get(cluster);
            else
                dataRead = currentCluster.read();
            System.arraycopy(dataRead, 0, data, pos + readLen, Math.min(dataRead.length, len - readLen));
            readLen += dataRead.length;
        }

        if(readLen > len)
            return len;
        return readLen;
    }

    public void flush() throws IOException {
        fileSystem.writeData(this);
    }

    @Override
    protected int readBytes(byte[] b, int off, int len) throws IOException {
        return readAbsolute(currentPos, b, off, len);
    }

    @Override
    protected void writeBytes(byte[] b, int off, int len) throws IOException {
        writeAbsolute(currentPos, b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int oldPos = currentPos;
        currentPos += n;
        return currentPos - oldPos;
    }

    @Override
    public int getFilePointer() throws IOException {
        return currentPos;
    }

    @Override
    public void seek(long pos) throws IOException {
        currentPos = (int)pos;
    }

    @Override
    public void close() throws IOException {
        flush();
    }
}
