import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CrackingServerInterface extends Remote {
    void startSearch(String targetHash, int passwordLength, char startChar, char endChar, int threadCount) throws RemoteException;
    boolean isPasswordFound() throws RemoteException;
    String getFoundPassword() throws RemoteException;
    long getSearchTime() throws RemoteException;
}
