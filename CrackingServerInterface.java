import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CrackingServerInterface extends Remote {
    boolean startSearch(String hash, int passwordLength, int serverId, int totalServers, int threadsPerServer) throws RemoteException;
    boolean isPasswordFound() throws RemoteException;
    String getPassword() throws RemoteException;
    String getFoundByThread() throws RemoteException;
    long getTimeSpent() throws RemoteException;
}
