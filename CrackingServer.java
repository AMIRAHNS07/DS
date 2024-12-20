import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.rmi.RemoteException;
import java.rmi.Remote
import java.util.concurrent.atomic.AtomicBoolean;

public class CrackingServer extends UnicastRemoteObject implements CrackingServerInterface {
    private volatile boolean found = false; // Flag to stop threads
    private String foundPassword = null;    // Holds the found password
    private long searchTime = 0;            // Total search time
    private long totalCombinations = 0;    // Total combinations to try
    private long checkedCombinations = 0;  // Combinations checked so far

    protected CrackingServer() throws Exception {
        super();
    }

    @Override
    public void startSearch(String targetHash, int passwordLength, char startChar, char endChar, int threadCount) throws RemoteException {
        System.out.println("Starting search on server...");
        long startTime = System.currentTimeMillis();

        Thread[] threads = new Thread[threadCount];
        int rangePerThread = (endChar - startChar + 1) / threadCount;

        totalCombinations = (long) Math.pow((endChar - startChar + 1), passwordLength);
        AtomicBoolean stopFlag = new AtomicBoolean(false);

        for (int i = 0; i < threadCount; i++) {
            final char rangeStart = (char) (startChar + i * rangePerThread);
            final char rangeEnd = (char) (i == threadCount - 1 ? endChar : rangeStart + rangePerThread - 1);

            threads[i] = new Thread(() -> bruteForceSearch(targetHash, passwordLength, rangeStart, rangeEnd, "", stopFlag));
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
    }

    private void bruteForceSearch(String targetHash, int length, char start, char end, String prefix, AtomicBoolean stopFlag) {
        if (found || stopFlag.get()) return;

        if (prefix.length() == length) {
            checkedCombinations++;
            String hashed = md5(prefix);
            if (hashed != null && hashed.equals(targetHash)) {
                found = true;
                stopFlag.set(true);
                foundPassword = prefix;
                System.out.println("Password found: " + prefix);
            }
            return;
        }

        for (char c = start; c <= end && !found && !stopFlag.get(); c++) {
            bruteForceSearch(targetHash, length, start, end, prefix + c, stopFlag);
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
    public double getProgress() throws RemoteException {
        return (double) checkedCombinations / totalCombinations * 100;
    }

    public static void main(String[] args) {
        try {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : 1099;
            Registry registry = LocateRegistry.createRegistry(port);
            CrackingServer server = new CrackingServer();
            registry.rebind("CrackingServer", server);
            System.out.println("Server started on port " + port);
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
