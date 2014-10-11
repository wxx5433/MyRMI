package DispatchNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Communication.RMIMessage;

/**
 * This class is used to initialize a socket thread for slave nodes to have a
 * socked connection with master node.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 *
 */
public class DispatchListenSocketThread implements Runnable {
	private DispatchNode dispatchNode;
	private int portNum;
	private boolean stop;

	public DispatchListenSocketThread(DispatchNode dispatchNode, int portNum) {
		this.dispatchNode = dispatchNode;
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
					RMIMessage invokeRequest = (RMIMessage) inputStream
							.readObject();
					// ObjectOutputStream outputStream = new ObjectOutputStream(
					// socket.getOutputStream());
					dispatchNode.newInvokeRequest(invokeRequest, socket);
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
