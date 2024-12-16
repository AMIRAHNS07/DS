import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrackingServer extends UnicastRemoteObject implements CrackingServerInterface {

    private volatile boolean found = false;
    private ExecutorService executor;

    public CrackingServer() throws RemoteException {
        super();
    }

    @Override
    public void startCracking(String targetHash, int passwordLength, char startChar, char endChar, int threadCount) throws RemoteException {
        System.out.println("Server started cracking with " + threadCount + " threads...");
        executor = Executors.newFixedThreadPool(threadCount);

        int range = (endChar - startChar + 1) / threadCount;
        for (int i = 0; i < threadCount; i++) {
            char rangeStart = (char) (startChar + i * range);
            char rangeEnd = (i == threadCount - 1) ? endChar : (char) (rangeStart + range - 1);

            executor.submit(new BruteForceTask(targetHash, passwordLength, rangeStart, rangeEnd));
        }
    }

    @Override
    public void stopCracking() throws RemoteException {
        found = true;
        executor.shutdownNow();
        System.out.println("Search stopped.");
    }

    private class BruteForceTask implements Runnable {
        private String targetHash;
        private int passwordLength;
        private char startChar, endChar;

        public BruteForceTask(String targetHash, int passwordLength, char startChar, char endChar) {
            this.targetHash = targetHash;
            this.passwordLength = passwordLength;
            this.startChar = startChar;
            this.endChar = endChar;
        }

        @Override
        public void run() {
            bruteForce("", passwordLength);
        }

        private void bruteForce(String current, int length) {
            if (found || current.length() == length) {
                if (current.length() == length && getMd5(current).equals(targetHash)) {
                    System.out.println("Password found: " + current + " by Thread " + Thread.currentThread().getName());
                    found = true;
                }
                return;
            }

            for (char c = startChar; c <= endChar && !found; c++) {
                bruteForce(current + c, length);
            }
        }

        private String getMd5(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] messageDigest = md.digest(input.getBytes());
                StringBuilder hexString = new StringBuilder();
                for (byte b : messageDigest) {
                    hexString.append(String.format("%02x", b));
                }
                return hexString.toString();
            } catch (Exception e) {
                return "";
            }
        }
    }
}
