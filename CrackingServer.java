import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicBoolean;

public class CrackingServer extends UnicastRemoteObject implements CrackingServerInterface {

    private static final long serialVersionUID = 1L;
    private AtomicBoolean isFound = new AtomicBoolean(false);
    private String foundPassword = null;
    private long totalCombinations;
    private long combinationsChecked = 0;

    public CrackingServer() throws RemoteException {
        super();
    }

    @Override
    public void startSearch(String targetHash, int passwordLength, int numThreads) throws RemoteException {
        this.totalCombinations = (long) Math.pow(95, passwordLength) / numThreads;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    searchPassword(targetHash, passwordLength, threadId, numThreads);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getResult() throws RemoteException {
        return foundPassword != null ? "Password: " + foundPassword : "Password not found";
    }

    @Override
    public boolean isPasswordFound() throws RemoteException {
        return isFound.get();
    }

    @Override
    public String getProgress() throws RemoteException {
        double progress = (combinationsChecked / (double) totalCombinations) * 100;
        return String.format("Progress: %.2f%%", progress);
    }

    private void searchPassword(String targetHash, int passwordLength, int threadId, int numThreads) {
        int startChar = 32 + (95 / numThreads) * threadId;
        int endChar = startChar + (95 / numThreads);
        if (threadId == numThreads - 1) endChar = 127;

        try {
            bruteForce("", passwordLength, targetHash, startChar, endChar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bruteForce(String current, int length, String targetHash, int startChar, int endChar) {
        if (isFound.get()) return;

        if (current.length() == length) {
            combinationsChecked++;
            if (getMd5(current).equals(targetHash)) {
                setFoundPassword(current);
            }
            return;
        }

        for (int i = startChar; i < endChar; i++) {
            if (isFound.get()) return;
            bruteForce(current + (char) i, length, targetHash, 32, 126);
        }
    }

    private synchronized void setFoundPassword(String password) {
        if (!isFound.get()) {
            foundPassword = password;
            isFound.set(true);
        }
    }

    private String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
