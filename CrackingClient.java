import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CrackingClient {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter MD5 hash value (or 'exit' to quit): ");
            String hash = scanner.nextLine();
            if (hash.equalsIgnoreCase("exit")) return;

            System.out.print("Enter password length: ");
            int passwordLength = scanner.nextInt();

            System.out.print("Enter number of servers (1-2): ");
            int totalServers = scanner.nextInt();

            System.out.print("Enter number of threads per server (1-10): ");
            int threadsPerServer = scanner.nextInt();

            Registry registry1 = LocateRegistry.getRegistry("192.168.1.1", 1099);
            CrackingServerInterface server1 = (CrackingServerInterface) registry1.lookup("CrackingServer");

            CrackingServerInterface server2 = null;
            if (totalServers == 2) {
                Registry registry2 = LocateRegistry.getRegistry("192.168.1.2", 1099);
                server2 = (CrackingServerInterface) registry2.lookup("CrackingServer");
            }

            long startTime = System.currentTimeMillis();

            if (server2 != null) {
                Thread server1Thread = new Thread(() -> {
                    try {
                        server1.startSearch(hash, passwordLength, 0, totalServers, threadsPerServer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                Thread server2Thread = new Thread(() -> {
                    try {
                        server2.startSearch(hash, passwordLength, 1, totalServers, threadsPerServer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                server1Thread.start();
                server2Thread.start();

                server1Thread.join();
                server2Thread.join();
            } else {
                server1.startSearch(hash, passwordLength, 0, totalServers, threadsPerServer);
            }

            long endTime = System.currentTimeMillis();
            long timeSpent = endTime - startTime;

            if (server1.isPasswordFound()) {
                System.out.println("Password found: " + server1.getPassword());
                System.out.println("Password found by " + server1.getFoundByThread());
                System.out.println("Time spent searching: " + timeSpent + "ms");
            } else if (server2 != null && server2.isPasswordFound()) {
                System.out.println("Password found: " + server2.getPassword());
                System.out.println("Password found by " + server2.getFoundByThread());
                System.out.println("Time spent searching: " + timeSpent + "ms");
            } else {
                System.out.println("Password not found.");
                System.out.println("Time spent searching: " + timeSpent + "ms");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
