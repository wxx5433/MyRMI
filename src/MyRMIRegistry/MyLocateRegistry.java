package MyRMIRegistry;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public abstract class MyLocateRegistry {
	private static final int DEFAULT_PORT_NUM = 11111;
	private static final String HOST_NAME = "128.237.163.210";
	
	public static RegistryCommunicator getRegistry() throws UnknownHostException, IOException {
		return getRegistry(HOST_NAME, DEFAULT_PORT_NUM);
	}
	
	
	public static RegistryCommunicator getRegistry(String hostName, int port) throws UnknownHostException, IOException {
		// test connectivity
		Socket socket = new Socket(hostName, port);
		socket.close();
		return new RegistryCommunicator(hostName, port);
	}
}
