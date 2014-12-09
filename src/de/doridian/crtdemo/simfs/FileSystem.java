package de.doridian.crtdemo.simfs;

import de.doridian.crtdemo.simfs.data.DirectoryData;
import de.doridian.crtdemo.simfs.data.FileData;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileSystem {
    public static int HEADER_SIZE = 1 + 2;

    public final int clusterSize; //unsigned byte
    public final int numClusters; //unsigned short

    private RandomAccessFile randomAccessFile;

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
            cluster.readAttributes(randomAccessFile);
            if(!cluster.isAttribute(Cluster.ATTRIBUTE_ALLOCATED)) {
                cluster.attributes = Cluster.ATTRIBUTE_ALLOCATED;
                return cluster;
            }
        }
        return null;
    }

    void deallocateCluster(Cluster cluster) throws IOException {
        cluster.readHead(randomAccessFile);
        if(!cluster.isAttribute(Cluster.ATTRIBUTE_ALLOCATED))
            return;
        if(cluster.nextCluster > 0)
            deallocateCluster(getCluster(cluster.nextCluster));
        cluster.setAttribute(Cluster.ATTRIBUTE_ALLOCATED, false);
        cluster.writeAttributes(randomAccessFile);
    }

    public AbstractData readData(int startCluster) throws IOException {
        Cluster firstCluster = getCluster(startCluster);
        firstCluster.read(randomAccessFile);
        if(!firstCluster.isAttribute(Cluster.ATTRIBUTE_FIRST))
            throw new IOException("Not a first cluster");

        AbstractData data;
        if(firstCluster.isAttribute(Cluster.ATTRIBUTE_DIRECTORY))
            data = new DirectoryData();
        else
            data = new FileData();
        data.name = new String(firstCluster.contents, "ASCII");

        data.attributeCluster = firstCluster;
        data.firstDataCluster = getCluster(firstCluster.nextCluster);

        return data;
    }

    public void deleteData(AbstractData data) throws IOException {
        if(data.attributeCluster != null)
            deallocateCluster(data.attributeCluster);

        if(data.firstDataCluster != null)
            deallocateCluster(data.firstDataCluster);

        data.attributeCluster = null;
        data.firstDataCluster = null;
        data.allDataDirty = true;
        data.attributesDirty = true;
    }

    public void writeData(AbstractData data) throws IOException {
        if(data.attributeCluster == null || data.firstDataCluster == null) {
            deleteData(data);

            data.attributeCluster = allocateCluster();
            data.attributeCluster.setAttribute(Cluster.ATTRIBUTE_FIRST, true);
            data.attributeCluster.setAttribute(data.getSetAttributes(), true);;
        }

        if(data.attributesDirty) {
            data.attributeCluster.readHead(randomAccessFile);
            data.attributeCluster.contents = data.name.getBytes("ASCII");
            data.attributeCluster.write(randomAccessFile);
        }

        if(data.allDataDirty || !data.dataClustersDirty.isEmpty()) {
            byte[] contents = data.getContents();
            int clusterCount = (int)Math.ceil(((double)contents.length) / ((double)clusterSize));

            Cluster currentCluster = data.attributeCluster;
            currentCluster.readHead(randomAccessFile);

            if(clusterCount < 1)
                clusterCount = 1;

            for(int clusterNumber = 0; clusterNumber < clusterCount; clusterNumber++) {
                boolean forceWriteThisCluster = false;

                if(currentCluster.nextCluster > 0) {
                    currentCluster = getCluster(currentCluster.nextCluster);
                    currentCluster.readHead(randomAccessFile);
                } else {
                    Cluster newCluster = allocateCluster();

                    if(currentCluster.location == data.attributeCluster.location)
                        data.firstDataCluster = newCluster;

                    currentCluster.nextCluster = newCluster.location;
                    currentCluster = newCluster;

                    forceWriteThisCluster = true;
                }

                if(data.allDataDirty || forceWriteThisCluster || data.dataClustersDirty.contains(clusterNumber)) {
                    int pos = clusterNumber * clusterSize;
                    int len = Math.min(clusterSize, contents.length - pos);
                    if (len < 0)
                        len = 0;
                    currentCluster.contents = new byte[len];
                    System.arraycopy(contents, pos, currentCluster.contents, 0, len);
                    currentCluster.write(randomAccessFile);
                }
            }
        }

        data.allDataDirty = false;
        data.attributesDirty = false;
        data.dataClustersDirty.clear();
    }
}
