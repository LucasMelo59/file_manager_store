package service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import model.DirectoryEntry;
import model.Inode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

@ApplicationScoped
public class FileManagerService {

    @Inject
    private VirtualDisk disk;
    @Inject
    private JournalService journal;

    private static final int ROOT_INODE_ID = 0;

    private static final String ENTRY_DELIMITER = "|";
    private static final String PART_DELIMITER = ":";


    private Inode getRootInode() {
        return disk.getInode(ROOT_INODE_ID);
    }


    private List<DirectoryEntry> readDirectory(Inode directoryInode) {
        List<DirectoryEntry> entries = new ArrayList<>();

        if (directoryInode == null || !directoryInode.isDirectory || directoryInode.directBlocks == null) {
            return entries;
        }

        int blockId = directoryInode.directBlocks[0];

        if (blockId != -1) {
            byte[] blockData = disk.dataBlocks[blockId];
            String blockContent = new String(blockData)
                    .split("\u0000")[0] // Pega apenas a parte antes do primeiro byte nulo
                    .trim();

            if (blockContent.isEmpty()) return entries;

            String[] parts = blockContent.split("\\" + ENTRY_DELIMITER);
            for (String part : parts) {
                if (part.contains(PART_DELIMITER)) {
                    try {
                        String[] pair = part.split(PART_DELIMITER);
                        if (pair.length == 2 && !pair[0].isEmpty()) {
                            entries.add(new DirectoryEntry(pair[0], Integer.parseInt(pair[1])));
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Erro ao parsear entrada de diretório: " + part);
                    }
                }
            }
        }
        return entries;
    }


    private void saveDirectory(Inode directoryInode, List<DirectoryEntry> entries) {
        if (directoryInode == null || !directoryInode.isDirectory) {
            System.err.println("Erro: Tentativa de salvar diretório em Inode inválido ou nulo.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (DirectoryEntry entry : entries) {
            sb.append(entry.toString()).append(ENTRY_DELIMITER);
        }
        String content = sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";

        System.out.println("DEBUG: Conteúdo do diretório a ser salvo: [" + content + "]");

        int blockId = directoryInode.directBlocks[0];
        if (blockId == -1) {
            blockId = disk.findFreeBlock();
            if (blockId == -1) {
                System.err.println("Erro: Sem blocos livres para salvar diretório.");
                return;
            }
            directoryInode.directBlocks[0] = blockId;
        }

        byte[] contentBytes = content.getBytes();
        int bytesToWrite = Math.min(contentBytes.length, VirtualDisk.BLOCK_SIZE);

        byte[] dataBlock = disk.dataBlocks[blockId];

        Arrays.fill(dataBlock, (byte) 0);

        System.arraycopy(contentBytes, 0, dataBlock, 0, bytesToWrite);

        directoryInode.lastModified = LocalDateTime.now();
        directoryInode.size = bytesToWrite; // Atualiza o tamanho
    }

    private DirectoryEntry findEntry(String name) {
        Inode root = getRootInode();
        if (root == null) return null;

        return readDirectory(root).stream()
                .filter(e -> e.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    // Método delete (rm)
    public void delete(String name) {
        DirectoryEntry entry = findEntry(name);
        if (entry == null) { System.out.println("Erro: Item não encontrado."); return; }

        journal.log("DELETE", name, entry.inodeId);

        Inode targetInode = disk.getInode(entry.inodeId);
        if (targetInode == null) {
            System.err.println("Erro: Inode alvo nulo ao deletar.");
            return;
        }

        if (targetInode.isDirectory && !readDirectory(targetInode).isEmpty()) {
            System.out.println("Erro: Diretório não vazio.");
            return;
        }

        for (int blockId : targetInode.directBlocks) {
            if (blockId != -1) disk.freeBlock(blockId);
        }

        disk.freeInode(entry.inodeId);

        Inode root = getRootInode();
        if (root == null) return;

        List<DirectoryEntry> entries = readDirectory(root);
        entries.removeIf(e -> e.name.equals(name));

        saveDirectory(root, entries);

        disk.saveDisk();
        journal.commit();
        System.out.println("Item deletado: " + name);
    }

    public void createDirectory(String dirName) {
        if (findEntry(dirName) != null) {
            System.out.println("Erro: Item já existe.");
            return;
        }
        System.out.println("entrando");
        journal.log("CREATE_DIR", dirName, -1);

        int newInodeId = disk.findFreeInode();
        System.out.println("oi");
        if (newInodeId == -1) { System.out.println("Erro: Sem Inodes livres."); return; }

        Inode newInode = new Inode(newInodeId, true); // true = diretório
        disk.inodes[newInodeId] = newInode;
        System.out.println("entrando");
        Inode root = getRootInode();
        if (root == null) return;

        List<DirectoryEntry> entries = readDirectory(root);
        entries.add(new DirectoryEntry(dirName, newInodeId));

        saveDirectory(root, entries);

        disk.saveDisk();
        journal.commit();
        System.out.println("Diretório criado: " + dirName);
    }

    // Método createFile (touch)
    public void createFile(String fileName, String content) {
        if (findEntry(fileName) != null) {
            System.out.println("Erro: Item já existe.");
            return;
        }

        journal.log("CREATE_FILE", fileName, -1);

        int newInodeId = disk.findFreeInode();
        int newBlockId = disk.findFreeBlock();

        if (newInodeId == -1 || newBlockId == -1) {
            System.out.println("Erro: Sem recursos (Inode ou Bloco) livres. Desalocando recursos alocados.");
            if (newInodeId != -1) disk.freeInode(newInodeId);
            if (newBlockId != -1) disk.freeBlock(newBlockId);
            return;
        }

        Inode newInode = new Inode(newInodeId, false);
        newInode.directBlocks[0] = newBlockId;
        newInode.size = content.length();
        disk.inodes[newInodeId] = newInode;

        byte[] contentBytes = content.getBytes();
        int bytesToWrite = Math.min(contentBytes.length, VirtualDisk.BLOCK_SIZE);

        Arrays.fill(disk.dataBlocks[newBlockId], (byte) 0);
        System.arraycopy(contentBytes, 0, disk.dataBlocks[newBlockId], 0, bytesToWrite);


        Inode root = getRootInode();
        if (root == null) return;

        List<DirectoryEntry> entries = readDirectory(root);
        entries.add(new DirectoryEntry(fileName, newInodeId));
        saveDirectory(root, entries);

        disk.saveDisk();
        journal.commit();
        System.out.println("Arquivo criado: " + fileName + " (" + bytesToWrite + " bytes)");
    }

    public void listDirectory() {
        Inode root = getRootInode();
        if (root == null) {
            System.out.println("Erro: I-node raiz não encontrado.");
            return;
        }

        System.out.println("\n=== Conteúdo do Diretório Raiz ===");
        List<DirectoryEntry> entries = readDirectory(root);

        if (entries.isEmpty()) {
            System.out.println("(Vazio)");
        }

        for (DirectoryEntry entry : entries) {
            Inode targetInode = disk.inodes[entry.inodeId];
            if (targetInode != null) {
                String type = targetInode.isDirectory ? "[DIR]" : "[FILE]";
                System.out.printf("%-5s %-20s (Inode: %d, Tamanho: %d bytes, Modificado: %s)\n",
                        type, entry.name, entry.inodeId, targetInode.size, targetInode.lastModified.toLocalTime());
            } else {
                System.err.printf("[ERRO] %s (Inode %d) com referência perdida!\n", entry.name, entry.inodeId);
            }
        }
        System.out.println("================================");
    }

    // Método rename (mv)
    public void rename(String oldName, String newName) {
        DirectoryEntry entry = findEntry(oldName);
        if (entry == null) { System.out.println("Erro: Item '" + oldName + "' não encontrado."); return; }
        if (findEntry(newName) != null) { System.out.println("Erro: Já existe item com o novo nome '" + newName + "'."); return; }

        journal.log("RENAME", oldName + " -> " + newName, entry.inodeId);

        Inode root = getRootInode();
        if (root == null) return;

        List<DirectoryEntry> entries = readDirectory(root);

        boolean found = false;
        for (DirectoryEntry e : entries) {
            if (e.name.equals(oldName)) {
                e.name = newName; // Renomeia
                found = true;
                break;
            }
        }

        if (!found) {
            System.err.println("Erro fatal: Item encontrado via findEntry, mas não na lista de leitura.");
            return;
        }

        saveDirectory(root, entries);

        Inode targetInode = disk.getInode(entry.inodeId);
        if (targetInode != null) {
            targetInode.lastModified = LocalDateTime.now();
        }

        disk.saveDisk();
        journal.commit();
        System.out.println("Renomeado de " + oldName + " para " + newName);
    }
}