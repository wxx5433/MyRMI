package RegistrationNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listen thread in <code>MasterNode</code>, it is launched when
 * <code>MasterNode</code> calls function start() It is responsible for waiting
 * connection with <code>SlaveNode</code>
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 *
 */
public class SocketListenThread implements Runnable {
	private RegistrationNode masterNode;
	private int portNum;
	private boolean stop;

	public SocketListenThread(RegistrationNode masterNode, int portNum) {
		this.masterNode = masterNode;
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
					String slaveName = (String) inputStream.readObject();
					ObjectOutputStream out = new ObjectOutputStream(
							socket.getOutputStream());
					System.out.println(slaveName + ":online!");
					masterNode.newSlaveOnline(slaveName, socket, out,
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
