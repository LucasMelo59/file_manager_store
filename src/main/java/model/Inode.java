package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

public class Inode implements Serializable {
    private static final int MAX_DIRECT_BLOCKS = 5;

    public int id;
    public boolean isDirectory;
    public int size;
    public LocalDateTime lastModified;
    public int[] directBlocks;

    public Inode(int id, boolean isDirectory) {
        this.id = id;
        this.isDirectory = isDirectory;
        this.lastModified = LocalDateTime.now();
        this.size = 0;
        this.directBlocks = new int[MAX_DIRECT_BLOCKS];
        for(int i=0; i < MAX_DIRECT_BLOCKS; i++) {
            this.directBlocks[i] = -1; // -1 indica bloco livre/não alocado
        }
    }
}