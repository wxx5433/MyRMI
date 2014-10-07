package MyRMIRegistry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import Remote.RemoteObjectReference;
import Communication.CommunicationMessage;
import Communication.CommunicationMessage.MessageType;

/**
 * Listen thread in <code>Registery</code>, it is launched when
 * <code>Registery</code> calls function start().
 * It is responsible for listening for lookup from client and 
 * register from server.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 *
 */
public class SocketListenThread implements Runnable {
	private RegistryServer registry;
	private int portNum;
	private transient boolean stop;

	public SocketListenThread(RegistryServer registryNode, int portNum) {
		this.registry = registryNode;
		this.portNum = portNum;
		stop = false;
	}

	@Override
	public void run() {
		ServerSocket listener;
		try {
			listener = new ServerSocket(portNum);
			while (!stop) {
				try {
					Socket socket = listener.accept();
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
					CommunicationMessage communicationMessage = 
							(CommunicationMessage) in.readObject();
					parseMessage(communicationMessage, socket);
				} catch (IOException e) {
					System.out.println("Error occur when listening:");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.out.println("Error accur when creating server:");
			e.printStackTrace();
		}
	}
	
	public void terminate() {
		stop = true;
	}
	
	public void parseMessage(CommunicationMessage message, Socket socket) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageType messageType = message.getMessageType();
		String messageContents = message.getMessage();
		CommunicationMessage replyMessage = null;
		// rebind a new service
		if (messageType == MessageType.NewService) {
			// message format: serviceName|IP|port
			String[] fields = messageContents.split("|");
			if (fields.length != 3) {
				replyMessage = new CommunicationMessage(
						MessageType.ReplyToServer, "Bad format when add service!");
				try {
					out.writeObject(replyMessage);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
System.out.println("Bad format when add service!");
				return;
			}
			String serviceName = fields[0];
			String ip = fields[1];
			int port = Integer.parseInt(fields[2]);
			replyMessage = registry.addService(serviceName, ip, port);
			try {
				out.writeObject(replyMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (messageType == MessageType.LookUpService){    // look up service
			String serviceName = messageContents;
			RemoteObjectReference ror = registry.lookup(serviceName);
			try {
				out.writeObject(ror);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
