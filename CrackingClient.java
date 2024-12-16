import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class CrackingClient {
    public static void main(String[] args) {
        try {
            // Connect to RMI Registry
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            CrackingServerInterface server = (CrackingServerInterface) registry.lookup("CrackingServer");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter MD5 hash to crack: ");
            String targetHash = scanner.nextLine();
            System.out.println("Enter password length: ");
            int passwordLength = scanner.nextInt();
            System.out.println("Enter number of threads: ");
            int numThreads = scanner.nextInt();

            // Start search
            server.startSearch(targetHash, passwordLength, numThreads);

            // Monitor progress
            while (!server.isPasswordFound()) {
                System.out.println(server.getProgress());
                Thread.sleep(1000);
            }

            // Display result
            System.out.println(server.getResult());

            scanner.close();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

