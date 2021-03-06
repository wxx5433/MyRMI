package MyRMIRegistry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import Communication.CommunicationMessage;
import Communication.CommunicationMessage.MessageType;
import Exception.MyRemoteException;
import Remote.RemoteObjectReference;

/**
 * This class is used by the server and the client to communicate with the
 * Registry server.
 * 
 * @author Xiaoxiang Wu (xiaoxiaw)
 * @author Ye Zhou (yezhou)
 */
public class RegistryCommunicator {
	/**
	 * RegistryServer's hostName and port
	 */
	private String hostName;
	private int port;

	public RegistryCommunicator(String hostName, int port) {
		this.hostName = hostName;
		this.port = port;
	}

	/**
	 * send clients' lookup request to the registry server
	 * 
	 * @param serviceName
	 *            the service name to lookup
	 * @return RemoteObjectReference of this service object
	 * @throws MyRemoteException
	 */
	public RemoteObjectReference lookup(String serviceName)
			throws MyRemoteException {
		RemoteObjectReference ror = null;
		try {
			// connect the registry server
			Socket socket = new Socket(hostName, port);
			// send the lookup request to the registry server
			CommunicationMessage message = new CommunicationMessage(
					MessageType.LookUpService, serviceName);
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			out.writeObject(message);
			// read result
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			ror = (RemoteObjectReference) in.readObject();
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ror;
	}

	/**
	 * send dispatch servers' new service request to the registry server.
	 * 
	 * @param serviceName
	 *            the new service name to rebind
	 * @param serverIp
	 *            dispatch server's ip
	 * @param serverPort
	 *            dispatch server's port
	 */
	public void rebind(String serviceName, String serverIp, int serverPort) {
		rebind(serviceName, serverIp, serverPort, -1);
	}

	/**
	 * send dispatch servers' new service request to the registry server.
	 * 
	 * @param serviceName
	 *            the new service name to rebind
	 * @param serverIp
	 *            dispatch server's ip
	 * @param serverPort
	 *            dispatch server's port
	 */
	public void rebind(String serviceName, String serverIp, int serverPort,
			long key) {
		try {
			// connect the registry server
			Socket socket = new Socket(hostName, port);
			// send the rebind request to the registry server
			CommunicationMessage message = new CommunicationMessage(
					MessageType.NewService, serviceName + " " + serverIp + " "
							+ serverPort + " " + key);
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			out.writeObject(message);
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			CommunicationMessage replyMessage = (CommunicationMessage) in
					.readObject();
			System.out.println(replyMessage.getMessage());
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}