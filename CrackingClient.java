import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CrackingClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
                System.out.print("Enter MD5 hash value (or 'exit' to quit): ");
                String targetHash = scanner.nextLine();
                if (targetHash.equalsIgnoreCase("exit")) {
                    break;
                }

                System.out.print("Enter password length: ");
                int passwordLength = scanner.nextInt();

                System.out.print("Enter number of servers (1-2): ");
                int serverCount = scanner.nextInt();

                System.out.print("Enter number of threads per server (1-10): ");
                int threadCount = scanner.nextInt();

                scanner.nextLine(); // Consume newline

                CrackingServerInterface[] servers = new CrackingServerInterface[2];
                Registry registry1 = LocateRegistry.getRegistry("192.168.122.101", 1099);
                servers[0] = (CrackingServerInterface) registry1.lookup("CrackingServer");

                if (serverCount == 2) {
                    Registry registry2 = LocateRegistry.getRegistry("192.168.122.102", 1099);
                    servers[1] = (CrackingServerInterface) registry2.lookup("CrackingServer");
                }

                long startTime = System.currentTimeMillis();

                Thread[] tasks = new Thread[serverCount];
                char midChar = 'M';

                tasks[0] = new Thread(() -> {
                    try {
                        servers[0].startSearch(targetHash, passwordLength, '!', midChar, threadCount, 1);
                    } catch (Exception e) {
                        System.err.println("Server 1 error: " + e.getMessage());
                    }
                });

                if (serverCount == 2) {
                    tasks[1] = new Thread(() -> {
                        try {
                            servers[1].startSearch(targetHash, passwordLength, (char) (midChar + 1), 'z', threadCount, 2);
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

                long elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println("Time spent: " + elapsedTime + "ms");

                boolean found = false;
                for (int i = 0; i < serverCount; i++) {
                    if (servers[i].isPasswordFound()) {
                        System.out.println("Password found: " + servers[i].getFoundPassword());
                        System.out.println("Found by Thread ID: " + servers[i].getThreadID());
                        found = true;
                    }
                }

                if (!found) {
                    System.out.println("Password not found.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        scanner.close();
    }
}
