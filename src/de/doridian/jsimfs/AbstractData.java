package de.doridian.jsimfs;

import de.doridian.jsimfs.data.DirectoryData;
import de.doridian.jsimfs.interfaces.IAbstractData;
import de.doridian.jsimfs.interfaces.IDirectoryData;
import de.doridian.jsimfs.interfaces.IFileSystem;

import java.io.Closeable;
import java.io.IOException;
import java.util.TreeMap;

public class AbstractData extends SimpleDataInputOutput implements Closeable, IAbstractData {
    public Cluster attributeCluster;
    protected final FileSystem fileSystem;
    String name;

    protected boolean autoFlush = false;

    protected TreeMap<Integer, byte[]> dataClustersDirty = new TreeMap<>();
    protected int lastClusterIndex = 0;
    protected boolean attributesDirty;

    private int currentPos = 0;

    public DirectoryData parent = null;

    public AbstractData(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        setAutoFlush(true);
    }

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

    @Override
    public String getAbsolutePath() {
        if(parent != null)
            return parent.getAbsolutePath() + FileSystem.PATH_SEPARATOR + getName();
        return "" + FileSystem.PATH_SEPARATOR;
    }

    int getClusterDataSize() {
        return fileSystem.clusterSize - Cluster.HEADER_SIZE;
    }

    private byte[] readCluster(int cluster) throws IOException {
        if(dataClustersDirty.containsKey(cluster))
            return dataClustersDirty.get(cluster);
        else
            return fileSystem.getCluster(cluster).read();
    }

    @Override
    public IDirectoryData getParent() throws IOException {
        return parent;
    }

    @Override
    public IFileSystem getFileSystem() {
        return fileSystem;
    }

    public void setLength(int len) throws IOException {
        seek(len);
        writeEOF();
    }

    public void writeEOF() throws IOException {
        int eofPos = currentPos - 1;
        if(eofPos < 0)
            eofPos = 0;
        int lastCluster = getClusterFor(eofPos);
        byte[] cluster = readCluster(lastCluster);
        byte[] endCluster = new byte[(eofPos % getClusterDataSize()) + 1];
        if(cluster.length == endCluster.length)
            endCluster = cluster;
        else
            System.arraycopy(cluster, 0, endCluster, 0, Math.min(endCluster.length, cluster.length));
        dataClustersDirty.put(lastCluster, endCluster);
        lastClusterIndex = lastCluster;

        Integer higherKey;
        while((higherKey = dataClustersDirty.higherKey(lastClusterIndex)) != null)
            dataClustersDirty.remove(higherKey);
    }

    protected void writeAbsolute(int filePos, byte[] data, int pos, int len) throws IOException {
        int startCluster = getClusterFor(filePos);
        int endCluster = getClusterFor(filePos + len - 1);
        int writtenData = 0;

        int oldPos = startCluster * getClusterDataSize();
        byte[] oldData = new byte[((int)Math.ceil(((double) (len + (filePos - oldPos))) / ((double) getClusterDataSize()))) * getClusterDataSize()];
        int oldLen = readAbsolute(oldPos, oldData, 0, oldData.length);

        for(int cluster = startCluster; cluster <= endCluster; cluster++) {
            int curLen = Math.min(getClusterDataSize(), len - writtenData);
            int curOldLen = Math.min(getClusterDataSize(), oldLen - writtenData);
            int curOffset = (cluster == startCluster) ? filePos % getClusterDataSize() : 0;

            byte[] clusterData = new byte[Math.max(curLen + curOffset, curOldLen)];

            if(curOldLen > 0)
                System.arraycopy(oldData, writtenData, clusterData, 0, curOldLen);
            if(curLen > 0)
                System.arraycopy(data, pos + writtenData, clusterData, curOffset, curLen);

            writtenData += curLen;
            dataClustersDirty.put(cluster, clusterData);
        }

        if(lastClusterIndex < endCluster)
            lastClusterIndex = endCluster;
    }

    public void delete() throws IOException {
        parent.deleteFile(this);
        fileSystem.deleteData(this);
    }

    public void moveTo(IDirectoryData directoryData) throws IOException {
        DirectoryData toDirectory = (DirectoryData)directoryData;
        toDirectory.addFile(this);
    }

    protected int readAbsolute(int filePos, byte[] data, int pos, int len) throws IOException {
        int startCluster = getClusterFor(filePos);
        int endCluster = getClusterFor(filePos + len);
        if(lastClusterIndex >= 0 && endCluster > lastClusterIndex)
            endCluster = lastClusterIndex;
        if(endCluster < startCluster)
            return 0;

        int readLen = 0;
        Cluster currentCluster = attributeCluster;
        for(int cluster = 0; cluster <= endCluster; cluster++) {
            if(currentCluster != null) {
                currentCluster.readHead();

                if (currentCluster.nextCluster <= 0)
                    currentCluster = null;
                else
                    currentCluster = fileSystem.getCluster(currentCluster.nextCluster);
            }

            if(cluster < startCluster)
                continue;

            byte[] dataRead;

            int cOffset = (cluster == startCluster) ? filePos % getClusterDataSize() : 0;
            int cMaxLen = len - readLen;

            if(dataClustersDirty.containsKey(cluster)) {
                byte[] dirtyCluster = dataClustersDirty.get(cluster);
                if(cOffset > dirtyCluster.length)
                    cOffset = dirtyCluster.length;
                dataRead = new byte[Math.min(dirtyCluster.length - cOffset, cMaxLen)];
                System.arraycopy(dirtyCluster, cOffset, dataRead, 0, dataRead.length);
            } else if(currentCluster != null) {
                dataRead = currentCluster.read(cOffset, cMaxLen);
            } else {
                break;
            }
            System.arraycopy(dataRead, 0, data, pos + readLen, dataRead.length);
            readLen += dataRead.length;
        }

        return readLen;
    }

    public void flush() throws IOException {
        fileSystem.writeData(this);
    }

    @Override
    protected int readBytes(byte[] b, int off, int len) throws IOException {
        int read = readAbsolute(currentPos, b, off, len);
        currentPos += read;
        return read;
    }

    @Override
    protected void writeBytes(byte[] b, int off, int len) throws IOException {
        writeAbsolute(currentPos, b, off, len);
        currentPos += len;
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
    public void seek(int pos) throws IOException {
        currentPos = pos;
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    @Override
    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
    }
}
