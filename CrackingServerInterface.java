import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CrackingServerInterface extends Remote {
    void startSearch(String targetHash, int passwordLength, int numThreads) throws RemoteException;
    String getResult() throws RemoteException;
    boolean isPasswordFound() throws RemoteException;
    String getProgress() throws RemoteException;
}
