import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CrackingClient {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter MD5 hash value: ");
            String targetHash = scanner.nextLine();

            System.out.print("Enter password length: ");
            int passwordLength = scanner.nextInt();

            System.out.print("Enter number of servers (1-2): ");
            int serverCount = scanner.nextInt();
            if (serverCount < 1 || serverCount > 2) {
                throw new IllegalArgumentException("Invalid number of servers (1-2 allowed).");
            }

            System.out.print("Enter number of threads per server (1-10): ");
            int threadCount = scanner.nextInt();
            if (threadCount < 1 || threadCount > 10) {
                throw new IllegalArgumentException("Invalid number of threads (1-10 allowed).");
            }

            CrackingServerInterface[] servers = new CrackingServerInterface[2];
            Registry registry1 = LocateRegistry.getRegistry("192.168.122.101", 1099);
            servers[0] = (CrackingServerInterface) registry1.lookup("CrackingServer");

            if (serverCount == 2) {
                Registry registry2 = LocateRegistry.getRegistry("192.168.122.102", 1100);
                servers[1] = (CrackingServerInterface) registry2.lookup("CrackingServer");
            }

            System.out.println("Starting distributed password search...");

            Thread[] tasks = new Thread[serverCount];
            char midChar = 'M';

            tasks[0] = new Thread(() -> {
                try {
                    servers[0].startSearch(targetHash, passwordLength, '!', midChar, threadCount);
                } catch (Exception e) {
                    System.err.println("Server 1 error: " + e.getMessage());
                }
            });

            if (serverCount == 2) {
                tasks[1] = new Thread(() -> {
                    try {
                        servers[1].startSearch(targetHash, passwordLength, (char) (midChar + 1), 'z', threadCount);
                    } catch (Exception e) {
                        System.err.println("Server 2 error: " + e.getMessage());
                    }
                });
            }

            for (Thread task : tasks) {
                if (task != null) task.start();
            }

            for (Thread task : tasks) {
                if (task != null) task.join();
            }

            for (int i = 0; i < serverCount; i++) {
                if (servers[i].isPasswordFound()) {
                    System.out.println("Password found by Server " + (i + 1) + ": " + servers[i].getFoundPassword());
                }
            }

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
