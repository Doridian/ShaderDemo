package de.doridian.jsimfs;

import com.google.common.collect.MapMaker;
import de.doridian.jsimfs.data.DirectoryData;
import de.doridian.jsimfs.data.FileData;
import de.doridian.jsimfs.interfaces.IAbstractData;
import de.doridian.jsimfs.interfaces.IDirectoryData;
import de.doridian.jsimfs.interfaces.IFileSystem;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ConcurrentMap;

public class FileSystem implements IFileSystem {
    public static char PATH_SEPARATOR = '\\';

    public static int HEADER_SIZE = 2 + 2;

    public final int clusterSize; //unsigned short
    public final int numClusters; //unsigned short

    private IDirectoryData cwd;

    final RandomAccessFile randomAccessFile;

    public final DirectoryData rootDirectory;

    private final ConcurrentMap<Integer, Cluster> clusterWeakHashMap = new MapMaker().weakValues().makeMap();

    private FileSystem(int clusterSize, int numClusters, RandomAccessFile file) throws IOException {
        this.clusterSize = clusterSize;
        this.numClusters = numClusters;
        this.randomAccessFile = file;
        file.setLength(HEADER_SIZE + (this.clusterSize * this.numClusters));
        file.seek(0);

        DirectoryData rootDirectory;
        try {
            rootDirectory = (DirectoryData)readData(null, 0);
        } catch (NotValidDataException e) {
            rootDirectory = new DirectoryData(this);
            rootDirectory.parent = null;
            rootDirectory.setName("ROOT");
            rootDirectory.flush();
        }

        this.rootDirectory = rootDirectory;
        this.cwd = rootDirectory;
    }

    @Override
    public int getClusterSize() {
        return clusterSize;
    }

    @Override
    public int getClusterCount() {
        return numClusters;
    }

    @Override
    public void setCWD(IDirectoryData cwd) throws IOException {
        if(cwd == null)
            this.cwd = rootDirectory;
        else
            this.cwd = cwd;
    }

    @Override
    public IDirectoryData getCWD() throws IOException {
        return cwd;
    }

    @Override
    public IAbstractData getFile(String name) throws IOException {
        IAbstractData baseDir;
        if(name.charAt(0) == PATH_SEPARATOR) {
            name = name.substring(1);
            baseDir = rootDirectory;
        } else
            baseDir = cwd;

        String[] pathComponents = name.split(PATH_SEPARATOR + "+");
        for(String pathComponent : pathComponents) {
            if(pathComponent.isEmpty() || pathComponent.equals("."))
                continue;
            if(pathComponent.equals(".."))
                baseDir = baseDir.getParent();
            else
                baseDir = ((IDirectoryData)baseDir).findFile(pathComponent);
        }

        return baseDir;
    }

    public IDirectoryData getRootDirectory() {
        return rootDirectory;
    }

    public static IFileSystem create(int clusterSize, int numClusters, RandomAccessFile file) throws IOException {
        if(clusterSize < 1 || numClusters < 1 || clusterSize > 65535 || numClusters > 65535)
            throw new IllegalArgumentException("ClusterSize or NumClusters out of range (1 - 65535)");
        file.setLength(HEADER_SIZE);
        file.seek(0);
        file.writeShort(clusterSize);
        file.writeShort(numClusters);
        return new FileSystem(clusterSize, numClusters, file);
    }

    public static IFileSystem read(RandomAccessFile file) throws IOException {
        file.seek(0);
        int clusterSize = file.readUnsignedShort();
        int numClusters = file.readUnsignedShort();
        if(clusterSize < 1 || numClusters < 1 || clusterSize > 65535 || numClusters > 65535)
            throw new IllegalArgumentException("ClusterSize or NumClusters out of range (1 - 65535)");
        return new FileSystem(clusterSize, numClusters, file);
    }

    void seekToCluster(Cluster cluster) throws IOException {
        randomAccessFile.seek(HEADER_SIZE + (cluster.location * clusterSize));
    }

    Cluster getCluster(int clusterNumber) throws IOException {
        if(clusterNumber < 0)
            return null;

        if(clusterWeakHashMap.containsKey(clusterNumber))
            return clusterWeakHashMap.get(clusterNumber);


        Cluster ret = new Cluster(clusterNumber, this);
        clusterWeakHashMap.put(clusterNumber, ret);
        return ret;
    }

    Cluster allocateCluster(boolean clean) throws IOException {
        for(int i = 0; i < numClusters; i++) {
            Cluster cluster = getCluster(i);
            cluster.readAttributes();
            if(!cluster.hasAllAttributes(Cluster.ATTRIBUTE_ALLOCATED)) {
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
        if(!cluster.hasAllAttributes(Cluster.ATTRIBUTE_ALLOCATED))
            return;
        if(cluster.nextCluster > 0)
            deallocateCluster(getCluster(cluster.nextCluster));
        cluster.setAttribute(Cluster.ATTRIBUTE_ALLOCATED, false);
        cluster.writeAttributes();

        clusterWeakHashMap.remove(cluster.location);
    }

    public static class NotValidDataException extends IOException {

    }

    public AbstractData readData(DirectoryData parent, int startCluster) throws IOException {
        Cluster firstCluster = getCluster(startCluster);
        firstCluster.readAttributes();
        if(!firstCluster.hasAllAttributes(Cluster.ATTRIBUTE_FIRST | Cluster.ATTRIBUTE_ALLOCATED))
            throw new NotValidDataException();

        AbstractData data;
        if(firstCluster.hasAllAttributes(Cluster.ATTRIBUTE_DIRECTORY))
            data = new DirectoryData(this);
        else
            data = new FileData(this);
        data.name = new String(firstCluster.read(), "ASCII");

        data.lastClusterIndex = -1;
        data.attributeCluster = firstCluster;
        data.parent = parent;

        return data;
    }

    void deleteData(AbstractData data) throws IOException {
        if(data.attributeCluster != null)
            deallocateCluster(data.attributeCluster);

        data.attributeCluster = null;
        data.attributesDirty = true;
        data.lastClusterIndex = 0;
        data.dataClustersDirty.clear();
    }

    void writeData(AbstractData data) throws IOException {
        if(data.attributeCluster == null) {
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
