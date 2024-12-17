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

            Registry registry1 = LocateRegistry.getRegistry("192.168.122.101", 1099);
            CrackingServerInterface server1 = (CrackingServerInterface) registry1.lookup("CrackingServer");

            Registry registry2 = null;
            CrackingServerInterface server2 = null;

            if (serverCount == 2) {
                registry2 = LocateRegistry.getRegistry("192.168.122.102", 1100);
                server2 = (CrackingServerInterface) registry2.lookup("CrackingServer");
            }

            System.out.println("Starting distributed password search...");

            Thread server1Task = new Thread(() -> {
                try {
                    server1.startSearch(targetHash, passwordLength, '!', 'M', threadCount);
                } catch (Exception e) {
                    System.err.println("Server 1 error: " + e.getMessage());
                }
            });

            Thread server2Task = null;
            if (server2 != null) {
                server2Task = new Thread(() -> {
                    try {
                        server2.startSearch(targetHash, passwordLength, 'N', 'z', threadCount);
                    } catch (Exception e) {
                        System.err.println("Server 2 error: " + e.getMessage());
                    }
                });
            }

            server1Task.start();
            if (server2Task != null) server2Task.start();

            server1Task.join();
            if (server2Task != null) server2Task.join();

            if (server1.isPasswordFound()) {
                System.out.println("Password found by Server 1: " + server1.getFoundPassword());
            }
            if (server2 != null && server2.isPasswordFound()) {
                System.out.println("Password found by Server 2: " + server2.getFoundPassword());
            }

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
