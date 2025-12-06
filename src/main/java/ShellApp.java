
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import service.FileManagerService;

import java.util.Scanner;

@ApplicationScoped
public class ShellApp {

    @Inject
    FileManagerService fs;

    public void runShell() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== SIMULADOR I-NODE (v2.0) ===");
        System.out.println("Comandos: mkdir <nome>, touch <nome> <conteudo>, ls, rm <nome>, mv <antigo> <novo>, exit");

        while (true) {
            System.out.print("$ root/> ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split(" ", 3);
            String command = parts[0];

            try {
                switch (command) {
                    case "mkdir":
                        if (parts.length > 1) fs.createDirectory(parts[1]);
                        break;
                    case "touch":
                        if (parts.length > 1) { // Garante que há pelo menos um nome de arquivo
                            String content = (parts.length > 2) ? parts[2] : ""; // Usa o conteúdo se existir, senão vazio
                            fs.createFile(parts[1], content);
                        } else {
                            System.out.println("Uso: touch <nome> [conteudo]");
                        }
                        break;
                    case "ls":
                        fs.listDirectory();
                        break;
                    case "rm":
                        if (parts.length > 1) fs.delete(parts[1]);
                        break;
                    case "mv":
                        if (parts.length > 2) fs.rename(parts[1], parts[2]);
                        break;
                    case "exit":
                        System.out.println("Encerrando e salvando estado do disco...");
                        // O saveDisk é chamado no final de cada operação no service
                        return;
                    default:
                        System.out.println("Comando desconhecido.");
                }
            } catch (Exception e) {
                System.err.println("Erro durante a operação: " + e.getMessage());
            }
        }
    }
}