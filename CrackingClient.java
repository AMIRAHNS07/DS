import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class CrackingServer extends UnicastRemoteObject implements CrackingServerInterface {
    private boolean passwordFound = false;
    private String foundPassword = null;
    private String foundByThread = null;
    private long timeSpent = 0;

    public CrackingServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {
            CrackingServer server = new CrackingServer();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("CrackingServer", server);
            System.out.println("Server started on port 1099");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public synchronized boolean startSearch(String hash, int passwordLength, int serverId, int totalServers, int threadsPerServer) throws RemoteException {
        System.out.println("Start searching....");
        long startTime = System.currentTimeMillis();

        int totalRange = 126 - 33 + 1; // ASCII printable range
        int rangePerServer = totalRange / totalServers;

        // Server range
        int serverStart = 33 + serverId * rangePerServer;
        int serverEnd = (serverId == totalServers - 1) ? 126 : serverStart + rangePerServer - 1;

        // Thread list
        List<Thread> threads = new ArrayList<>();

        // Divide work among threads
        int rangePerThread = (serverEnd - serverStart + 1) / threadsPerServer;

        for (int threadId = 0; threadId < threadsPerServer; threadId++) {
            int threadStart = serverStart + threadId * rangePerThread;
            int threadEnd = (threadId == threadsPerServer - 1) ? serverEnd : threadStart + rangePerThread - 1;

            Thread thread = new Thread(new PasswordCracker(threadStart, threadEnd, passwordLength, hash, threadId + 1, this));
            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        timeSpent = endTime - startTime;
        System.out.println("Stop searching...");
        return passwordFound;
    }

    @Override
    public synchronized boolean isPasswordFound() throws RemoteException {
        return passwordFound;
    }

    @Override
    public synchronized String getPassword() throws RemoteException {
        return foundPassword;
    }

    @Override
    public synchronized String getFoundByThread() throws RemoteException {
        return foundByThread;
    }

    @Override
    public synchronized long getTimeSpent() throws RemoteException {
        return timeSpent;
    }

    public synchronized void reportPasswordFound(String password, int threadId) {
        if (!passwordFound) {
            passwordFound = true;
            foundPassword = password;
            foundByThread = "Thread ID: " + threadId;
        }
    }

    static class PasswordCracker implements Runnable {
        private final int start;
        private final int end;
        private final int passwordLength;
        private final String hash;
        private final int threadId;
        private final CrackingServer server;

        public PasswordCracker(int start, int end, int passwordLength, String hash, int threadId, CrackingServer server) {
            this.start = start;
            this.end = end;
            this.passwordLength = passwordLength;
            this.hash = hash;
            this.threadId = threadId;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                char[] charset = new char[end - start + 1];
                for (int i = 0; i < charset.length; i++) {
                    charset[i] = (char) (start + i);
                }

                bruteForce("", passwordLength, charset);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void bruteForce(String prefix, int length, char[] charset) {
            if (length == 0) {
                if (server.isPasswordFound()) return;
                String hashToCheck = md5Hash(prefix);
                if (hash.equals(hashToCheck)) {
                    server.reportPasswordFound(prefix, threadId);
                }
                return;
            }

            for (char c : charset) {
                bruteForce(prefix + c, length - 1, charset);
            }
        }

        private String md5Hash(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hashBytes = md.digest(input.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
