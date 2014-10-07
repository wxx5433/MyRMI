package MyRMIRegistry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import Communication.CommunicationMessage;
import Communication.CommunicationMessage.MessageType;
import Exception.RemoteException;
import Remote.RemoteObjectReference;

/**
 * 
 * @author Xiaoxiang Wu (xiaoxiaw)
 * @author Ye Zhou (yezhou)
 *
 * This class is used by the server and the client to communicate with 
 * the Registry server.
 */
public class RegistryCommunicator {
	/**
	 * RegistryServer's hostName and port
	 */
	private String hostName;   
	private int port;

	
	public RegistryCommunicator (String hostName, int port) {
		this.hostName = hostName;
		this.port = port;
	}
	
	public RemoteObjectReference lookup(String serviceName) throws RemoteException {
		RemoteObjectReference ror = null;
		try {
			// connect the registry server
			Socket socket = new Socket(hostName, port);
			// send the lookup request to the registry server
			CommunicationMessage message = new CommunicationMessage(
					MessageType.LookUpService, serviceName);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(message);
			// read result
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			ror = (RemoteObjectReference) in.readObject();
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
	
	public void rebind(String serviceName, String serverIp, int serverPort) {
		try {
			// connect the registry server
			Socket socket = new Socket(hostName, port);
			// send the rebind request to the registry server
			CommunicationMessage message = new CommunicationMessage(
					MessageType.NewService, serviceName + "|" + serverIp + "|" + serverPort);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(message);
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
