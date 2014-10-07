package MyRMIRegistry;

public abstract class LocateRegistry {
	public static RegistryCommunicator getRegistry(String hostName, int port) {
		return new RegistryCommunicator(hostName, port);
	}
}
