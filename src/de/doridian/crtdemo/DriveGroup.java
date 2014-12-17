package de.doridian.crtdemo;

import de.doridian.jsimfs.FileSystem;
import de.doridian.jsimfs.interfaces.IAbstractData;
import de.doridian.jsimfs.interfaces.IFileSystem;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.TreeMap;

public class DriveGroup {
    public char currentDrive = 'C';
    public TreeMap<Character, IFileSystem> drives = new TreeMap<>();

    private final File folder;

    private static class FSFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.length() == 4 && name.toLowerCase().endsWith(".fs");
        }
    }

    public DriveGroup(File folder) throws IOException {
        this.folder = folder;

        for(File file : folder.listFiles(new FSFilenameFilter())) {
            if(file.isDirectory())
                continue;
            drives.put(file.getName().charAt(0), FileSystem.read(new RandomAccessFile(file, "rw")));
        }
    }

    public IFileSystem getDrive(char driveLetter) {
        return drives.get(driveLetter);
    }

    public TreeMap<Character, IFileSystem> getDrives() {
        return drives;
    }

    public IAbstractData getFile(String fileName) throws IOException {
        char useDrive = currentDrive;

        if(fileName.length() >= 3) {
            char driveLetter = fileName.substring(0, 1).toUpperCase().charAt(0);
            if(driveLetter >= 'A' && driveLetter <= 'Z' && fileName.charAt(1) == ':' && fileName.charAt(2) == FileSystem.PATH_SEPARATOR) {
                fileName = fileName.substring(2);
                useDrive = driveLetter;
            }
        }

        return getDrive(useDrive).getFile(fileName);
    }
}
