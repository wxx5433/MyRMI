package RegistrationNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Communication.CommunicationMessage;

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
					InputStream input = socket.getInputStream();
					ObjectInputStream inputStream = new ObjectInputStream(input);
					// receive message
					CommunicationMessage communicationMessage = 
							(CommunicationMessage) inputStream.readObject();
					
					
					
//					String slaveName = (String) inputStream.readObject();
					ObjectOutputStream out = new ObjectOutputStream(
							socket.getOutputStream());
					System.out.println(slaveName + ":online!");
					registry.newSlaveOnline(slaveName, socket, out,
							inputStream);
				} catch (IOException e) {
					System.out.println("Error occur when listening:");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
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
