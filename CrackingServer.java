import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrackingServer extends UnicastRemoteObject implements CrackingServerInterface {
    private volatile boolean found = false; // Flag to stop threads
    private String foundPassword = null;    // Holds the found password
    private long searchTime = 0;
    private long foundThreadID = -1;        // Thread ID that finds the password
    private int serverID;                   // Server ID

    protected CrackingServer(int serverID) throws RemoteException {
        super();
        this.serverID = serverID;
    }

    @Override
    public void startSearch(String targetHash, int passwordLength, char startChar, char endChar, int threadCount, int serverID) throws RemoteException {
        System.out.println("Start searching....");
        System.out.println("- Timestamp of start searching: " + getCurrentTimestamp());

        long startTime = System.currentTimeMillis();

        Thread[] threads = new Thread[threadCount];
        int rangePerThread = (endChar - startChar + 1) / threadCount;

        for (int i = 0; i < threadCount; i++) {
            final char rangeStart = (char) (startChar + i * rangePerThread);
            final char rangeEnd = (char) (i == threadCount - 1 ? endChar : rangeStart + rangePerThread - 1);

            threads[i] = new Thread(() -> bruteForceSearch(targetHash, passwordLength, rangeStart, rangeEnd, ""));
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }

        searchTime = System.currentTimeMillis() - startTime;
        System.out.println("- Timestamp of end searching: " + getCurrentTimestamp());
        System.out.println("Stop searching...");

        if (found) {
            System.out.println("- Password found by Server " + serverID + ".");
        }
    }

    private void bruteForceSearch(String targetHash, int length, char start, char end, String prefix) {
        if (found) return;

        if (prefix.length() == length) {
            String hashed = md5(prefix);
            if (hashed != null && hashed.equals(targetHash)) {
                found = true;
                foundPassword = prefix;
                foundThreadID = Thread.currentThread().getId();
                System.out.println("Password found: " + prefix);
            }
            return;
        }

        int progressInterval = 1000;  // Report progress every 1000 combinations
        int counter = 0;

        for (char c = start; c <= end && !found; c++) {
            bruteForceSearch(targetHash, length, start, end, prefix + c);
            counter++;

            if (counter % progressInterval == 0) {
                System.out.println("Thread Progress: " + counter + " combinations processed in range [" + start + "-" + end + "]");
            }
        }
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("MD5 algorithm not found: " + e.getMessage());
            return "";
        }
    }

    @Override
    public boolean isPasswordFound() throws RemoteException {
        return found;
    }

    @Override
    public String getFoundPassword() throws RemoteException {
        return foundPassword;
    }

    @Override
    public long getSearchTime() throws RemoteException {
        return searchTime;
    }

    @Override
    public long getThreadID() throws RemoteException {
        return foundThreadID;
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static void main(String[] args) {
        try {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
            int serverID = args.length > 1 ? Integer.parseInt(args[1]) : 1;
            Registry registry = LocateRegistry.createRegistry(port);
            CrackingServer server = new CrackingServer(serverID);
            registry.rebind("CrackingServer", server);
            System.out.println("Server " + serverID + " started on port " + port);
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
