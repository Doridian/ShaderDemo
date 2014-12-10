package de.doridian.crtdemo.simfs;

import de.doridian.crtdemo.simfs.data.DirectoryData;
import de.doridian.crtdemo.simfs.data.FileData;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileSystem {
    public static int HEADER_SIZE = 1 + 2;

    public final int clusterSize; //unsigned byte
    public final int numClusters; //unsigned short

    final RandomAccessFile randomAccessFile;

    public final DirectoryData rootDirectory;

    private FileSystem(int clusterSize, int numClusters, RandomAccessFile file, boolean rootDirectoryExists) throws IOException {
        this.clusterSize = clusterSize;
        this.numClusters = numClusters;
        this.randomAccessFile = file;
        file.setLength(HEADER_SIZE + (this.clusterSize * this.numClusters));
        file.seek(0);

        DirectoryData rootDirectory;
        try {
            rootDirectory = (DirectoryData)readData(0);
        } catch (NotValidDataException e) {
            rootDirectory = new DirectoryData(this);
            rootDirectory.setName("ROOT");
            rootDirectory.flush();
        }

        this.rootDirectory = rootDirectory;
    }

    public static FileSystem create(int clusterSize, int numClusters, RandomAccessFile file) throws IOException {
        file.setLength(HEADER_SIZE);
        file.seek(0);
        file.writeByte(clusterSize);
        file.writeShort(numClusters);
        return new FileSystem(clusterSize, numClusters, file, false);
    }

    public static FileSystem read(RandomAccessFile file) throws IOException {
        file.seek(0);
        int clusterSize = file.readUnsignedByte();
        int numClusters = file.readUnsignedShort();
        return new FileSystem(clusterSize, numClusters, file, true);
    }

    void seekToCluster(Cluster cluster) throws IOException {
        randomAccessFile.seek(HEADER_SIZE + (cluster.location * clusterSize));
    }

    Cluster getCluster(int clusterNumber) throws IOException {
        if(clusterNumber < 0)
            return null;

        return new Cluster(clusterNumber, this);
    }

    Cluster allocateCluster(boolean clean) throws IOException {
        for(int i = 0; i < numClusters; i++) {
            Cluster cluster = getCluster(i);
            cluster.readAttributes();
            if(!cluster.isAttribute(Cluster.ATTRIBUTE_ALLOCATED)) {
                cluster.attributes = Cluster.ATTRIBUTE_ALLOCATED;
                cluster.nextCluster = 0;
                if(clean)
                    cluster.write(new byte[0]);
                else
                    cluster.writeHead();
                return cluster;
            }
        }
        return null;
    }

    void deallocateCluster(Cluster cluster) throws IOException {
        cluster.readHead();
        if(!cluster.isAttribute(Cluster.ATTRIBUTE_ALLOCATED))
            return;
        if(cluster.nextCluster > 0)
            deallocateCluster(getCluster(cluster.nextCluster));
        cluster.setAttribute(Cluster.ATTRIBUTE_ALLOCATED, false);
        cluster.writeAttributes();
    }

    public static class NotValidDataException extends IOException {

    }

    public AbstractData readData(int startCluster) throws IOException {
        Cluster firstCluster = getCluster(startCluster);
        firstCluster.readAttributes();
        if(!firstCluster.isAttribute(Cluster.ATTRIBUTE_FIRST | Cluster.ATTRIBUTE_ALLOCATED))
            throw new NotValidDataException();

        AbstractData data;
        if(firstCluster.isAttribute(Cluster.ATTRIBUTE_DIRECTORY))
            data = new DirectoryData(this);
        else
            data = new FileData(this);
        data.name = new String(firstCluster.read(), "ASCII");

        data.attributeCluster = firstCluster;

        return data;
    }

    public void deleteData(AbstractData data) throws IOException {
        if(data.attributeCluster != null)
            deallocateCluster(data.attributeCluster);

        data.attributeCluster = null;
        data.attributesDirty = true;
        data.lastClusterIndex = 0;
        data.dataClustersDirty.clear();
    }

    public void writeData(AbstractData data) throws IOException {
        if(data.attributeCluster == null) {
            deleteData(data);

            data.attributesDirty = true;
            data.attributeCluster = allocateCluster(false);
            data.attributeCluster.setAttribute(Cluster.ATTRIBUTE_FIRST, true);
            data.attributeCluster.setAttribute(data.getSetAttributes(), true);
        }

        if(data.attributesDirty) {
            data.attributeCluster.readHead();
            data.attributeCluster.write(data.name.getBytes("ASCII"));
        }

        if(!data.dataClustersDirty.isEmpty()) {
            int clusterCount = data.dataClustersDirty.lastKey() + 1;

            Cluster currentCluster = data.attributeCluster;
            currentCluster.readHead();

            if(clusterCount < 1)
                clusterCount = 1;

            for(int clusterNumber = 0; clusterNumber < clusterCount; clusterNumber++) {
                boolean forceWriteThisCluster = false;
                byte[] clusterData = data.dataClustersDirty.containsKey(clusterNumber) ? data.dataClustersDirty.get(clusterNumber) : null;

                if(currentCluster.nextCluster > 0) {
                    currentCluster = getCluster(currentCluster.nextCluster);
                    currentCluster.readHead();
                    if(clusterNumber > data.lastClusterIndex) {
                        deallocateCluster(currentCluster);
                        break;
                    }
                } else {
                    if(clusterNumber > data.lastClusterIndex)
                        break;

                    Cluster newCluster = allocateCluster(false);
                    currentCluster.readHead();
                    currentCluster.nextCluster = newCluster.location;
                    currentCluster.writeHead();

                    currentCluster = newCluster;

                    forceWriteThisCluster = true;
                }

                if(clusterData != null)
                    currentCluster.write(data.dataClustersDirty.get(clusterNumber));
                else if(forceWriteThisCluster)
                    currentCluster.write(new byte[data.getClusterDataSize()]);
            }
        }

        data.attributesDirty = false;
        data.dataClustersDirty.clear();
    }
}
