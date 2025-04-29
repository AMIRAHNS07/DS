Java Multithreading MD5 Password Search
Project Description:
This project implements a Java multithreaded program to perform a brute-force search for passwords by matching their MD5 hash values. It runs on a single machine, using multiple threads to speed up the search.

How to Run:
1. Compile the Program: javac *.java
2. Execute the Program: java MD5Crack
3. CLI Inputs Required
   You will be prompted to input:
   - MD5 hash to crack.
   - Password length (between 3–6).
   - Number of threads (between 1–10)

Example Input:
Enter target MD5 hash: 5d41402abc4b2a76b9719d911017c592
Enter password length (3-6): 5
Enter number of threads (1-10): 4

Example Output:
Starting search...
Password found: hello
Thread ID: 2
Total Time: 12.5 seconds
