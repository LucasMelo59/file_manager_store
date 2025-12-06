package service;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.*;
import java.util.Date;

@ApplicationScoped
public class JournalService {
    private static final String JOURNAL_FILE = "journal.log";

    public void log(String operation, String target, int inodeId) {
        try (FileWriter fw = new FileWriter(JOURNAL_FILE, true);
             PrintWriter out = new PrintWriter(fw)) {
            out.printf("%s | OP: %s | TARGET: %s | INODE: %d\n",
                    new Date(), operation, target, inodeId);
        } catch (IOException e) {
            System.err.println("CRÍTICO: Falha no Journaling: " + e.getMessage());
        }
    }

    public void commit() {

        try {
            new PrintWriter(JOURNAL_FILE).close();
        } catch (FileNotFoundException ignored) {}
    }
}