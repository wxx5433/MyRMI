package DispatchNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Communication.CommunicationMessage;
import Communication.CommunicationMessage.MessageType;
import Utils.NodeID;

/**
 * This class is used to initialize a socket thread for slave nodes to have a
 * socked connection with master node.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 *
 */
public class DispatchServiceSocketThread implements Runnable {
	private NodeID registrationNodeID;
	private boolean stop;
	private boolean newMessage;
	private CommunicationMessage message;
	private DispatchNode dispatchNode;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public DispatchServiceSocketThread(DispatchNode slaveNode,
			NodeID masterNodeID) {
		this.dispatchNode = slaveNode;
		this.registrationNodeID = masterNodeID;
		stop = false;
		newMessage = false;
	}

	@Override
	public void run() {
		Socket socket;
		try {
			/* connect the newly started dispatch node to registration node */
			socket = new Socket(registrationNodeID.getHostName(),
					registrationNodeID.getPort());
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			/* tell registration node that the new slave node is online! */
			SendOnlineInfo(socket);

			/* Start receiving registration node's reply */
			inputStream = new ObjectInputStream(socket.getInputStream());
			Object recievedData = inputStream.readObject();
			message = (CommunicationMessage) recievedData;
			dispatchNode.recieveFeedBack(message);
			while (!stop) {
				/* receive commands from terminal and send it to registration */
				this.wait(2000);
				if (!newMessage) {
					message = new CommunicationMessage(MessageType.HeartBeat,
							this.dispatchNode.toString());
				}
				outputStream.writeObject(message);
				outputStream.reset();
				recievedData = inputStream.readObject();
				message = (CommunicationMessage) recievedData;
				dispatchNode.recieveFeedBack(message);
				System.out.println(message.getMessage());
				newMessage = false;
			}
			socket.close();
		} catch (IOException e) {
			System.out.println(dispatchNode.getDispatchName()
					+ ":Exception founded when trying to connect to manager: "
					+ e);
			e.printStackTrace();
			System.exit(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Tell the master node that a new slave node has logged in.
	 * 
	 * @param sock
	 * @throws IOException
	 */
	private void SendOnlineInfo(Socket sock) throws IOException {
		CommunicationMessage newDispatchOnline = new CommunicationMessage(
				MessageType.NewDispatchOnline, dispatchNode.getDispatchName());
		try {
			outputStream.writeObject(newDispatchOnline);
			outputStream.reset();
			System.out.println(dispatchNode.getDispatchName() + ":online");
		} catch (java.net.ConnectException e) {
			sock.close();
			System.out.println(dispatchNode.getDispatchName()
					+ ":Unable to get online to the registration!");
			System.exit(0);
		}
	}

	public void setNewMessage(CommunicationMessage message) {
		newMessage = true;
		this.message = message;
	}

	public void terminate() {
		stop = true;
	}

}
