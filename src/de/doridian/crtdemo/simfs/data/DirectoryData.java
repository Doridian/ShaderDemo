package de.doridian.crtdemo.simfs.data;

import de.doridian.crtdemo.simfs.AbstractData;
import de.doridian.crtdemo.simfs.Cluster;
import de.doridian.crtdemo.simfs.FileSystem;
import de.doridian.crtdemo.simfs.interfaces.IAbstractData;
import de.doridian.crtdemo.simfs.interfaces.IDirectoryData;
import de.doridian.crtdemo.simfs.interfaces.IFileData;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

public class DirectoryData extends AbstractData implements IDirectoryData {
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
                AbstractData data = fileSystem.readData(this, nextCluster);
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
            return getFilePointer() + (getFilePointer() % 2);
        return ret;
    }

    private int getFileLocation(AbstractData file) throws IOException {
        if(file.attributeCluster == null)
            return -1;

        return getClusterLocation(file.attributeCluster.location);
    }

    public void deleteFile(AbstractData file) throws IOException {
        int fileLocation = getFileLocation(file);
        if(fileLocation >= 0) {
            seek(fileLocation);
            writeShort(0);
        }
    }

    public void addFile(AbstractData file, String name) throws IOException {
        file.setName(name);
        addFile(file);
    }

    public void addFile(AbstractData file) throws IOException {
        if(getFileLocation(file) >= 0)
            return;

        if(file.parent != null)
            file.parent.deleteFile(file);
        file.parent = this;

        if(file.attributeCluster == null)
            file.flush();

        seek(getFreeLocation());
        writeShort(file.attributeCluster.location);
    }

    @Override
    public IFileData createFile(String name) throws IOException {
        FileData newFile = new FileData(fileSystem);
        addFile(newFile, name);
        return newFile;
    }

    @Override
    public IDirectoryData createDirectory(String name) throws IOException {
        DirectoryData newFile = new DirectoryData(fileSystem);
        addFile(newFile, name);
        return newFile;
    }

    public IAbstractData[] listFiles() throws IOException {
        seek(0);
        ArrayList<IAbstractData> files = new ArrayList<>();
        try {
            int nextCluster;
            while ((nextCluster = readUnsignedShort()) != -1) {
                if(nextCluster == 0)
                    continue;
                files.add(fileSystem.readData(this, nextCluster));
            }
        } catch (EOFException e) { }
        return files.toArray(new IAbstractData[files.size()]);
    }

    public void compact() throws IOException {
        IAbstractData[] allFiles = listFiles();
        seek(0);
        for(IAbstractData file : allFiles)
            writeShort(((AbstractData)file).attributeCluster.location);
        writeEOF();
    }

    @Override
    public void delete() throws IOException {
        seek(0);
        if(read() > 0)
            throw new IOException("Not empty");
        super.delete();
    }

    @Override
    public void flush() throws IOException {
        if((!dataClustersDirty.isEmpty()) && attributeCluster != null)
            compact();

        super.flush();
    }
}
