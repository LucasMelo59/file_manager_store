package service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import model.Inode;
import model.DirectoryEntry;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList; // Adicionar se necessário

// Adicione as anotações do CDI/Quarkus se aplicável
@ApplicationScoped
public class VirtualDisk implements Serializable{

    // ... (campos MAX_INODES, MAX_BLOCKS, dataBlocks, freeBlocks, etc.)
    public static final int MAX_INODES = 100;
    public static final int MAX_BLOCKS = 1000;
    public static final int BLOCK_SIZE = 128;

    public Inode[] inodes = new Inode[MAX_INODES];
    public byte[][] dataBlocks = new byte[MAX_BLOCKS][BLOCK_SIZE];
    public boolean[] freeBlocks = new boolean[MAX_BLOCKS];
    public boolean[] freeInodes = new boolean[MAX_INODES];

    private static final String DISK_FILE = "filesystem.dat"; // Nome do arquivo de persistência

    @PostConstruct
    void init() {
        if (!loadDisk()) {
            initializeNewDisk();
        }
    }

    private void initializeNewDisk() {
        Arrays.fill(freeBlocks, true);
        Arrays.fill(freeInodes, true);

        inodes[0] = new Inode(0, true);
        freeInodes[0] = false;
        System.out.println("Disco virtual criado e formatado com sucesso.");
    }


    public Inode getInode(int id) {
        if (id >= 0 && id < MAX_INODES) {
            System.out.println("estou retornando corretamente");
            return inodes[id];
        }
        return null;
    }

    public void saveDisk() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DISK_FILE))) {
            // Serializa a instância atual do VirtualDisk
            oos.writeObject(this);
            System.out.println("Estado do disco salvo em " + DISK_FILE);
        } catch (IOException e) {
            System.err.println("CRÍTICO: Erro ao salvar o estado do disco: " + e.getMessage());
        }
    }

    public boolean loadDisk() {
        File file = new File(DISK_FILE);
        if (!file.exists())
            return false;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            VirtualDisk loadedDisk = (VirtualDisk) ois.readObject();

            // Copia o estado carregado para a instância atual
            this.inodes = loadedDisk.inodes;
            this.dataBlocks = loadedDisk.dataBlocks;
            this.freeBlocks = loadedDisk.freeBlocks;
            this.freeInodes = loadedDisk.freeInodes;
            System.out.println("Estado do disco carregado de " + DISK_FILE);
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao carregar o estado do disco, reformatando: " + e.getMessage());
            return false;
        }
    }

    public int findFreeInode() {
        for (int i = 1; i < MAX_INODES; i++) { // Começa do 1, pois 0 é ROOT
            if (freeInodes[i]) {
                freeInodes[i] = false;
                return i;
            }
        }
        return -1;
    }

    public void freeInode(int id) {
        if (id >= 1 && id < MAX_INODES) {
            freeInodes[id] = true;
            inodes[id] = null; // Limpa a referência
        }
    }

    public int findFreeBlock() {
        for (int i = 0; i < MAX_BLOCKS; i++) {
            if (freeBlocks[i]) {
                freeBlocks[i] = false;
                return i;
            }
        }
        return -1;
    }

    public void freeBlock(int id) {
        if (id >= 0 && id < MAX_BLOCKS) {
            freeBlocks[id] = true;
            Arrays.fill(dataBlocks[id], (byte) 0);
        }
    }
}