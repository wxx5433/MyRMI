package RegistrationNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Utils.NodeID;
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
	private RegistrationNode registry;
	private int portNum;
	private transient boolean stop;

	public SocketListenThread(RegistrationNode registryNode, int portNum) {
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
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					CommunicationMessage communicationMessage = 
							(CommunicationMessage) in.readObject();
					MessageType messageType = communicationMessage.getMessageType();
					// new server online
					if (messageType == MessageType.NewDispatchOnline) {
						// message format: NodeID.toString()
						NodeID nodeID = NodeID.fromString(communicationMessage.getMessage());
						registry.newServerOnline(nodeID, socket, in, out);
						
						// start a new thread to handle afterward communication between registry and server
						new Thread(new SocketHandler(registry, socket, in, out)).start();
					} else if (messageType == MessageType.LookUpService){    // look up service
						registry.lookup();
					}
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
	
}
