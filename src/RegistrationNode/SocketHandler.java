package RegistrationNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Utils.NodeID;
import Utils.ServiceID;
import Communication.CommunicationMessage;
import Communication.CommunicationMessage.MessageType;

public class SocketHandler implements Runnable {
	
	private RegistrationNode registry = null;
	private Socket socket = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	
	public SocketHandler(RegistrationNode registry, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
		this.registry = registry;
		this.socket = socket;
		this.in = in;
		this.out = out;
	}
	
	public void parseMessage(CommunicationMessage message) throws Exception {
		String messageString = message.getMessage();
		MessageType messageType = message.getMessageType();
		if (messageType == MessageType.NewService) {   // new service
			registry.rebind(messageString, out);
		} else if (messageType == MessageType.DelService) {  // delete a service
			registry.deleteService(ServiceID.fromString(messageString), out);
		} else if (messageType == MessageType.HeartBeat) {   // heart beat from the server

		}
			
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				// block here to read message from the server or client
				CommunicationMessage message = (CommunicationMessage) in.readObject();
				parseMessage(message);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
