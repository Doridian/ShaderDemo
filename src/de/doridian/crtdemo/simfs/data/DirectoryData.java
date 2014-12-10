package de.doridian.crtdemo.simfs.data;

import de.doridian.crtdemo.simfs.AbstractData;
import de.doridian.crtdemo.simfs.Cluster;
import de.doridian.crtdemo.simfs.FileSystem;
import org.lwjgl.Sys;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

public class DirectoryData extends AbstractData {
    public DirectoryData(FileSystem fileSystem) {
        super(fileSystem);
    }

    @Override
    protected int getSetAttributes() {
        return Cluster.ATTRIBUTE_DIRECTORY;
    }

    public AbstractData findFile(String fileName) throws IOException {
        seek(0);
        try {
            int nextCluster;
            while ((nextCluster = readUnsignedShort()) != -1) {
                if(nextCluster == 0)
                    continue;
                AbstractData data = fileSystem.readData(nextCluster);
                if(data.getName().equals(fileName))
                    return data;
            }
        } catch (EOFException e) { }
        return null;
    }

    private int getClusterLocation(int cluster) throws IOException {
        seek(0);
        try {
            int nextCluster;
            while ((nextCluster = readUnsignedShort()) != -1) {
                if(nextCluster == cluster)
                    return getFilePointer() - 2;
            }
        } catch (EOFException e) { }
        return -1;
    }

    private int getFreeLocation() throws IOException {
        int ret = getClusterLocation(0);
        if(ret < 0)
            return getFilePointer();
        return ret;
    }

    private int getFileLocation(AbstractData file) throws IOException {
        if(file.attributeCluster == null)
            file.flush();
        return getClusterLocation(file.attributeCluster.location);
    }

    public void deleteFile(AbstractData file) throws IOException {
        int fileLocation = getFileLocation(file);
        if(fileLocation >= 0) {
            seek(fileLocation);
            writeShort(0);
        }
    }

    public void addFile(AbstractData file) throws IOException {
        if(getFileLocation(file) >= 0)
            return;
        if(file.attributeCluster == null)
            file.flush();
        seek(getFreeLocation());
        writeShort(file.attributeCluster.location);
    }

    public AbstractData[] listFiles() throws IOException {
        seek(0);
        ArrayList<AbstractData> files = new ArrayList<>();
        try {
            int nextCluster;
            while ((nextCluster = readUnsignedShort()) != -1) {
                if(nextCluster == 0)
                    continue;
                files.add(fileSystem.readData(nextCluster));
            }
        } catch (EOFException e) { }
        return files.toArray(new AbstractData[files.size()]);
    }
}
