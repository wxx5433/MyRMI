package RegistrationNode;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Utils.NodeID;
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
		if (message.getMessageType() == MessageType.NewService) {
			
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			
		}
	}

}
