import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CrackingClient {
    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("Usage: java CrackingClient <server1_ip> <server2_ip> <hash> <password_length> <threads_per_server>");
            return;
        }

        String server1Ip = args[0];
        String server2Ip = args[1];
        String targetHash = args[2];
        int passwordLength = Integer.parseInt(args[3]);
        int threadsPerServer = Integer.parseInt(args[4]);

        try {
            Registry registry1 = LocateRegistry.getRegistry(server1Ip);
            Registry registry2 = LocateRegistry.getRegistry(server2Ip);

            CrackingServerInterface server1 = (CrackingServerInterface) registry1.lookup("CrackingServer");
            CrackingServerInterface server2 = (CrackingServerInterface) registry2.lookup("CrackingServer");

            char startChar = '!';
            char midChar = (char) (startChar + (94 / 2));
            char endChar = '~';

            server1.startSearch(targetHash, passwordLength, startChar, midChar, threadsPerServer);
            server2.startSearch(targetHash, passwordLength, (char) (midChar + 1), endChar, threadsPerServer);

            while (!server1.isPasswordFound() && !server2.isPasswordFound()) {
                System.out.printf("Progress - Server 1: %.2f%%, Server 2: %.2f%%\n", server1.getProgress(), server2.getProgress());
                Thread.sleep(1000); // Update progress every second
            }

            CrackingServerInterface winnerServer = server1.isPasswordFound() ? server1 : server2;
            String foundPassword = winnerServer.getFoundPassword();
            long searchTime = winnerServer.getSearchTime();

            System.out.printf("Password Found: %s\nThread ID: %s\nServer: %s\nTime: %02d:%02d:%02d:%03d\n",
                    foundPassword, Thread.currentThread().getId(), (winnerServer == server1 ? "Server 1" : "Server 2"),
                    searchTime / 3600000, (searchTime / 60000) % 60, (searchTime / 1000) % 60, searchTime % 1000);

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
