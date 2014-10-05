package DispatchNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import MigratableProcess.MigratableProcess;
import Utils.NodeID;

/**
 * This class is used to initialize a socket thread for slave nodes to have a
 * socked connection with master node.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 *
 */
public class DispatchExecutionThread implements Runnable {
	private NodeID masterNodeID;
	private boolean stop;
	private DispatchNode slaveNode;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public DispatchServiceSocketThread(DispatchNode slaveNode, NodeID masterNodeID) {
		this.slaveNode = slaveNode;
		this.masterNodeID = masterNodeID;
		stop = false;
	}

	@Override
	public void run() {
		Socket socket;
		try {
			/* connect the newly started slave node to master node */
			socket = new Socket(masterNodeID.getHostName(),
					masterNodeID.getPort());
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			/* tell master node that the new slave node is online! */
			SendOnlineInfo(socket);

			/* Start receiving master's command */
			inputStream = new ObjectInputStream(socket.getInputStream());
			while (!stop) {
				/* receive commands from masterNode */
				Object recievedData = inputStream.readObject();
				if (recievedData instanceof String) {
					String command = (String) recievedData;
					/* get feedback of the command executed on slave node */
					Object feedback = slaveNode.executeCommand(command);
					/* send feedback to master node */
					outputStream.writeObject(feedback);
					outputStream.reset();
				} else if (recievedData instanceof MigratableProcess) {
					/*
					 * This slave node is asked to execute a process migrated
					 * from other slave node
					 */
					MigratableProcess migratableProcess = (MigratableProcess) recievedData;
					Object feedback = slaveNode
							.launchMigratedProcess(migratableProcess);
					/* send feedback to master node */
					outputStream.writeObject(feedback);
					outputStream.reset();
				}
			}
			socket.close();
		} catch (IOException e) {
			System.out.println(slaveNode.getDispatchName()
					+ ":Exception founded when trying to connect to manager: "
					+ e);
			e.printStackTrace();
			System.exit(0);
		} catch (ClassNotFoundException e) {
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
		try {
			outputStream.writeObject(slaveNode.getDispatchName());
			outputStream.reset();
			System.out.println(slaveNode.getDispatchName() + ":Registerred");
		} catch (java.net.ConnectException e) {
			sock.close();
			System.out.println(slaveNode.getDispatchName()
					+ ":Unable to register to the manager!");
			System.exit(0);
		}
	}

	public void terminate() {
		stop = true;
	}

}
