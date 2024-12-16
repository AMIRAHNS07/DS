import java.rmi.Naming;
import java.util.Scanner;

public class CrackingClient {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // Get user inputs
            System.out.print("Enter MD5 hash to crack: ");
            String targetHash = scanner.nextLine();

            System.out.print("Enter password length: ");
            int passwordLength = scanner.nextInt();

            System.out.print("Enter number of servers (1 or 2): ");
            int serverCount = scanner.nextInt();

            System.out.print("Enter number of threads per server: ");
            int threadCount = scanner.nextInt();

            // Input validation
            if (serverCount < 1 || serverCount > 2 || threadCount < 1 || threadCount > 10) {
                System.out.println("Error: Server count must be 1-2, and thread count must be 1-10.");
                return;
            }

            // Connect to servers
            CrackingServerInterface[] servers = new CrackingServerInterface[serverCount];
            for (int i = 0; i < serverCount; i++) {
                servers[i] = (CrackingServerInterface) Naming.lookup("//localhost/Server" + (i + 1));
                System.out.println("Connected to Server " + (i + 1));
            }

            // Distribute search space
            char startChar = '!';
            char endChar = '~';
            int range = (endChar - startChar + 1) / serverCount;

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < serverCount; i++) {
                char rangeStart = (char) (startChar + i * range);
                char rangeEnd = (i == serverCount - 1) ? endChar : (char) (rangeStart + range - 1);

                servers[i].startCracking(targetHash, passwordLength, rangeStart, rangeEnd, threadCount);
            }

            System.out.println("Password cracking started...");

            // Stop servers after result
            for (CrackingServerInterface server : servers) {
                server.stopCracking();
            }

            long endTime = System.currentTimeMillis();
            System.out.printf("Time spent: %.2f seconds%n", (endTime - startTime) / 1000.0);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
