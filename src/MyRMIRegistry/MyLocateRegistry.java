package MyRMIRegistry;

public abstract class MyLocateRegistry {
	private static final int DEFAULT_PORT_NUM = 11111;
	private static final String HOST_NAME = "128.237.163.210";
	
	public static RegistryCommunicator getRegistry() {
		return getRegistry(HOST_NAME, DEFAULT_PORT_NUM);
	}
	
	
	public static RegistryCommunicator getRegistry(String hostName, int port) {
		return new RegistryCommunicator(hostName, port);
	}
}
