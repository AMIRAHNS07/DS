import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class CrackingClient {
    public static void main(String[] args) {
        try {
            // Predefined server IP addresses
            String[] serverIPs = {"192.168.122.101", "192.168.122.102"};
            int serverCount = 1;  // Set the number of servers (1 or 2)
            int threadCount = 5;  // Set the number of threads per server (1-10)

            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter MD5 hash value: ");
            String targetHash = scanner.nextLine();

            System.out.print("Enter password length: ");
            int passwordLength = scanner.nextInt();

            // RMI lookup for servers
            CrackingServerInterface[] servers = new CrackingServerInterface[2];

            Registry registry1 = LocateRegistry.getRegistry(serverIPs[0], 1099);
            servers[0] = (CrackingServerInterface) registry1.lookup("CrackingServer");

            if (serverCount == 2) {
                Registry registry2 = LocateRegistry.getRegistry(serverIPs[1], 1099);
                servers[1] = (CrackingServerInterface) registry2.lookup("CrackingServer");
            }

            System.out.println("Starting distributed password search...");

            String timeStart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            System.out.println("Start Time: " + timeStart);

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

            // Display results
            for (int i = 0; i < serverCount; i++) {
                if (servers[i].isPasswordFound()) {
                    String foundPassword = servers[i].getFoundPassword();
                    System.out.println("Password Found: " + foundPassword);
                    System.out.println("Thread ID: " + servers[i].getThreadID());
                    System.out.println("Server: Server " + (i + 1));
                    long totalTime = servers[i].getSearchTime();
                    System.out.println("Total Time: " + totalTime / 1000.0 + " seconds");
                    System.out.println("Start Time: " + timeStart);
                    String timeEnd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                    System.out.println("End Time: " + timeEnd);
                }
            }

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
