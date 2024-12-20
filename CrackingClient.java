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
                Registry registry2 = LocateRegistry.getRegistry("192.168.122.102", 1099);
                servers[1] = (CrackingServerInterface) registry2.lookup("CrackingServer");
            }

            System.out.println("Starting distributed password search...");
            System.out.println("Password is ...........");

            // Capture start time for time tracking
            long startTime = System.currentTimeMillis();

            Thread[] tasks = new Thread[serverCount];
            char midChar = 'M';

            // Multithreading: Create threads for each server to perform the search
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

            // Start all threads
            for (Thread task : tasks) {
                if (task != null) task.start();
            }

            // Wait for all threads to complete
            for (Thread task : tasks) {
                if (task != null) task.join();
            }

            // Capture the end time and calculate elapsed time
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            // Format time spent
            long hours = (elapsedTime / (1000 * 60 * 60)) % 24;
            long minutes = (elapsedTime / (1000 * 60)) % 60;
            long seconds = (elapsedTime / 1000) % 60;
            long milliseconds = elapsedTime % 1000;
            String formattedTime = String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, milliseconds);

            // Check if any server found the password
            for (int i = 0; i < serverCount; i++) {
                if (servers[i].isPasswordFound()) {
                    System.out.println("Password found by Server " + (i + 1) + ": " + servers[i].getFoundPassword());
                    System.out.println("Password found by Thread ID: " + servers[i].getThreadID());
                }
            }

            System.out.println("Time spent: " + formattedTime);

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
