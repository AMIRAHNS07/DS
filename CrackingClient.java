import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CrackingClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // User inputs
        System.out.println("Enter the MD5 hash value to crack:");
        String targetHash = scanner.nextLine().trim();

        if (targetHash.isEmpty() || targetHash.length() != 32) {
            System.out.println("Error: Invalid MD5 hash. It must be a 32-character hexadecimal string.");
            return;
        }

        System.out.println("Enter the number of threads per server (1–10):");
        int threadsPerServer = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Enter the password length (3–6):");
        int passwordLength = Integer.parseInt(scanner.nextLine().trim());

        System.out.println("Enter the number of servers (1–2):");
        int serverCount = Integer.parseInt(scanner.nextLine().trim());

        scanner.close();

        try {
            long totalStartTime = System.currentTimeMillis();
            boolean passwordFound = false;

            for (int i = 1; i <= serverCount; i++) {
                String serverName = "CrackingServer" + i;
                Registry registry = LocateRegistry.getRegistry("localhost", 1099 + i);
                CrackingServerInterface server = (CrackingServerInterface) registry.lookup(serverName);

                System.out.println("Starting search on " + serverName);
                server.startSearch(targetHash, passwordLength, threadsPerServer);
            }

            while (!passwordFound) {
                for (int i = 1; i <= serverCount; i++) {
                    String serverName = "CrackingServer" + i;
                    Registry registry = LocateRegistry.getRegistry("localhost", 1099 + i);
                    CrackingServerInterface server = (CrackingServerInterface) registry.lookup(serverName);

                    System.out.println(serverName + ": " + server.getProgress());

                    if (server.isPasswordFound()) {
                        String result = server.getResult();
                        System.out.println("Password found by " + serverName + ": " + result);
                        passwordFound = true;
                        break;
                    }
                }

                Thread.sleep(2000); // Poll every 2 seconds
            }

            long totalEndTime = System.currentTimeMillis();
            long totalTime = (totalEndTime - totalStartTime) / 1000; // Time in seconds
            System.out.println("Total time spent: " + totalTime + " seconds");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
