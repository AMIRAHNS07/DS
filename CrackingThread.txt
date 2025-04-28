public class CrackingThread extends Thread {

    private String targetHash;
    private int passwordLength;
    private int numThreads;
    private int threadId;

    public CrackingThread(String targetHash, int passwordLength, int numThreads, int threadId) {
        this.targetHash = targetHash;
        this.passwordLength = passwordLength;
        this.numThreads = numThreads;
        this.threadId = threadId;
    }

    @Override
    public void run() {
        // (d) Divide search space based on thread ID
        int startChar = 32 + (95 / numThreads) * threadId;
        int endChar = startChar + (95 / numThreads);

        // Ensure last thread checks the full range up to ASCII 126
        if (threadId == numThreads - 1) {
            endChar = 127;
        }

        try {
            bruteForce("", passwordLength, startChar, endChar);
        } catch (Exception e) {
            System.out.println("Error in thread " + threadId + ": " + e.getMessage());
        }
    }

    private void bruteForce(String current, int length, int startChar, int endChar) {
        if (MD5Crack.isPasswordFound()) return;

        if (current.length() == length) {
            if (MD5Crack.getMd5(current).equals(targetHash)) {
                MD5Crack.setFoundPassword(current);
                System.out.println("Password found by Thread " + (threadId + 1) + " : " + current);
            }
            return;
        }

        // Generate combinations recursively by appending characters
        for (int i = startChar; i < endChar; i++) {
            if (MD5Crack.isPasswordFound()) return;
            char ch = (char) i;
            bruteForce(current + ch, length, 32, 126);  // Recursively check all combinations for each character
        }
    }
}