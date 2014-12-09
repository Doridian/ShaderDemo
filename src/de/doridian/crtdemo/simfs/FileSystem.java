package de.doridian.crtdemo.simfs;

import de.doridian.crtdemo.simfs.data.DirectoryData;
import de.doridian.crtdemo.simfs.data.FileData;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileSystem {
    public static int HEADER_SIZE = 1 + 2;

    public final int clusterSize; //unsigned byte
    public final int numClusters; //unsigned short

    RandomAccessFile randomAccessFile;

    public final DirectoryData rootDirectory;

    public FileSystem(short clusterSize, int numClusters, DirectoryData rootDirectory) {
        this.clusterSize = clusterSize;
        this.numClusters = numClusters;
        this.rootDirectory = rootDirectory;
    }

    void seekToCluster(Cluster cluster) throws IOException {
        randomAccessFile.seek(HEADER_SIZE + (cluster.location * clusterSize));
    }

    Cluster getCluster(int clusterNumber) throws IOException {
        if(clusterNumber < 0)
            return null;

        return new Cluster(clusterNumber, this);
    }

    Cluster allocateCluster() throws IOException {
        for(int i = 1; i < numClusters; i++) {
            Cluster cluster = getCluster(i);
            cluster.readAttributes();
            if(!cluster.isAttribute(Cluster.ATTRIBUTE_ALLOCATED)) {
                cluster.attributes = Cluster.ATTRIBUTE_ALLOCATED;
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

    public AbstractData readData(int startCluster) throws IOException {
        Cluster firstCluster = getCluster(startCluster);
        firstCluster.read();
        if(!firstCluster.isAttribute(Cluster.ATTRIBUTE_FIRST))
            throw new IOException("Not a first cluster");

        AbstractData data;
        if(firstCluster.isAttribute(Cluster.ATTRIBUTE_DIRECTORY))
            data = new DirectoryData();
        else
            data = new FileData();
        data.name = new String(firstCluster.read(), "ASCII");

        data.fileSystem = this;

        data.attributeCluster = firstCluster;

        return data;
    }

    public void deleteData(AbstractData data) throws IOException {
        data.fileSystem = this;

        if(data.attributeCluster != null)
            deallocateCluster(data.attributeCluster);

        data.attributeCluster = null;
        data.attributesDirty = true;
        if(data.dataClustersDirty.isEmpty())
            data.dataClustersDirty.put(0, new byte[0]);
    }

    public void writeData(AbstractData data) throws IOException {
        data.fileSystem = this;

        if(data.attributeCluster == null) {
            deleteData(data);

            data.attributeCluster = allocateCluster();
            data.attributeCluster.setAttribute(Cluster.ATTRIBUTE_FIRST, true);
            data.attributeCluster.setAttribute(data.getSetAttributes(), true);;
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
                boolean hasClusterData = data.dataClustersDirty.containsKey(clusterNumber);

                if(currentCluster.nextCluster > 0) {
                    currentCluster = getCluster(currentCluster.nextCluster);
                    currentCluster.readHead();
                } else {
                    Cluster newCluster = allocateCluster();

                    currentCluster.nextCluster = newCluster.location;
                    currentCluster = newCluster;

                    forceWriteThisCluster = true;
                }

                if(forceWriteThisCluster || hasClusterData) {
                    if(hasClusterData)
                        currentCluster.write(data.dataClustersDirty.get(clusterNumber));
                    else
                        currentCluster.writeHead();
                }
            }
        }

        data.attributesDirty = false;
        data.dataClustersDirty.clear();
    }
}
