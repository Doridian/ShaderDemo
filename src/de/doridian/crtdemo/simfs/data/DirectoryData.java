package de.doridian.crtdemo.simfs.data;

import de.doridian.crtdemo.simfs.AbstractData;
import de.doridian.crtdemo.simfs.Cluster;

public class DirectoryData extends AbstractData {
    @Override
    protected int getSetAttributes() {
        return Cluster.ATTRIBUTE_DIRECTORY;
    }
}
