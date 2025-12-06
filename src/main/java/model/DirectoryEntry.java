package model;

import java.io.Serializable;

public class DirectoryEntry implements Serializable {
    public String name;
    public int inodeId;

    public DirectoryEntry(String name, int inodeId) {
        this.name = name;
        this.inodeId = inodeId;
    }

    @Override
    public String toString() {
        return name + ":" + inodeId;
    }
}