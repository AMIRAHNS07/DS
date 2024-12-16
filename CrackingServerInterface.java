import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CrackingServerInterface extends Remote {
    void startCracking(String targetHash, int passwordLength, char startChar, char endChar, int threadCount) throws RemoteException;
    void stopCracking() throws RemoteException;
}
