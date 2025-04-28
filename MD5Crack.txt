import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class MD5Crack {

    private static AtomicBoolean isFound = new AtomicBoolean(false);
    private static String foundPassword = null;
    private static long startTime;
    private static long endTime;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // (a) CLI inputs with error handling
        System.out.println("Enter target MD5 hash: ");
        String targetHash = scanner.nextLine().trim();

        if (targetHash.isEmpty()) {
            System.out.println("Error: MD5 hash cannot be empty.");
            return;
        }

        int passwordLength = 0;
        while (passwordLength < 3 || passwordLength > 6) {
            System.out.println("Enter password length (3-6): ");
            try {
                passwordLength = Integer.parseInt(scanner.nextLine().trim());
                if (passwordLength < 3 || passwordLength > 6) {
                    System.out.println("Error: Password length must be between 3 and 6.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a number between 3 and 6.");
            }
        }

        int numThreads = 0;
        while (numThreads < 1 || numThreads > 10) {
            System.out.println("Enter number of threads (1-10): ");
            try {
                numThreads = Integer.parseInt(scanner.nextLine().trim());
                if (numThreads < 1 || numThreads > 10) {
                    System.out.println("Error: Number of threads must be between 1 and 10.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid input. Please enter a number between 1 and 10.");
            }
        }

        // Set start time with formatted timestamp
        startTime = System.currentTimeMillis();
        LocalDateTime startDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("Start time: " + startDateTime.format(formatter));

        // (d) Create and start threads
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new CrackingThread(targetHash, passwordLength, numThreads, i);
            threads[i].start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Set end time with formatted timestamp
        endTime = System.currentTimeMillis();
        LocalDateTime endDateTime = LocalDateTime.now();
        System.out.println("End time: " + endDateTime.format(formatter));

        // Display results based on whether the password was found
        if (isFound.get()) {
            System.out.println("Password found: " + foundPassword);
            System.out.println("Time taken: " + (endTime - startTime) / 1000.0 + " seconds");
        } else {
            System.out.println("Password not found within the given length and character set.");
        }

        scanner.close();
    }

    // Method to calculate MD5 hash
    public static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) hashtext = "0" + hashtext;
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Method to update found status and password
    public static synchronized void setFoundPassword(String password) {
        foundPassword = password;
        isFound.set(true);
    }

    public static boolean isPasswordFound() {
        return isFound.get();
    }
}
