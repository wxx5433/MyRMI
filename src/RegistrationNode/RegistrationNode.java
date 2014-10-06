package RegistrationNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import DispatchNode.DisTerminalThread;
import Utils.NodeID;
import Utils.ServiceID;

/**
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 *
 */
public class RegistrationNode {
	/**
	 * store unique serviceId, which refers to a remote object service on the 
	 * server specified by NodeID.
	 */
	private Map<ServiceID, NodeID> remoteObjectTable = null;
	/**
	 * Indicates if a node is healthy.
	 * Key is the server's ID, value is the last time registry receive heartBeart from the server.
	 */
	private Map<NodeID, Long> healthTable = null;
	/**
	 * Map use to maintain socket connection with server
	 */
	private Map<NodeID, Socket> socketTable = null;
	private Map<Socket, ObjectInputStream> inputStreamTable = null;
	private Map<Socket, ObjectOutputStream> outputStreamTable = null;
	
	private int portNum;
	private static final int DEFAULT_PORT_NUM = 11111;
	SocketListenThread socketListener;
	Thread listen = null;

	public RegistrationNode() {
		this(DEFAULT_PORT_NUM);
	}

	public RegistrationNode(int portNum) {
		this.portNum = portNum;
		socketListener = null;
		listen = null;
		remoteObjectTable = new ConcurrentHashMap<ServiceID, NodeID>();
		healthTable = new ConcurrentHashMap<NodeID, Long>();
		socketTable = new ConcurrentHashMap<NodeID, Socket>();
		inputStreamTable = new ConcurrentHashMap<Socket, ObjectInputStream>();
		outputStreamTable = new ConcurrentHashMap<Socket, ObjectOutputStream>();
	}

	/**
	 * Start the listen thread 
	 */
	private void start() {
		/* start a listen thread to accept socket connection */
		socketListener = new SocketListenThread(this, this.portNum);
		listen = new Thread(socketListener);
		listen.start();
	}

	/**
	 * If one new slave is connected to master node
	 * 
	 * @param slaveName
	 * @param socket
	 * @param out
	 * @param input
	 */
	public void newDispatchOnline(String slaveName, Socket socket,
			ObjectOutputStream out, ObjectInputStream input) {
		NodeID slaveNodeID = NodeID.fromString(slaveName);
		slavesManagement.put(slaveNodeID, socket);
		slavesOutputMap.put(slaveNodeID, out);
		slavesInputMap.put(slaveNodeID, input);
		System.out.println(slaveNodeID.toString() + " add to management!");
		processManager.newSlaveOnline(slaveNodeID);
	}

	/**
	 * Parse users' input command
	 * 
	 * @param command
	 */
	public void parseCommand(String command) {
		System.out.println(command + " recieved!");
		if (command.startsWith("launch")) {
			if (commandAvailable())
				launchNewProcess(command);
		} else if (command.startsWith("targetlaunch")) {
			if (commandAvailable())
				targetLaunchNewProcess(command);
		} else if (command.startsWith("migrate")) {
			if (commandAvailable())
				migrateProcess(command);
		} else if (command.startsWith("list")) {
			listStatus();
		} else if (command.startsWith("terminate")) {
			if (commandAvailable())
				terminateSlave(command);
		} else if (command.startsWith("processterminate")) {
			if (commandAvailable())
				terminateProcess(command);
		} else if (command.equals("exit")) {
			stop();
		} else {
			System.out.println("input error, no such command: " + command
					+ " please check and input the correct!");
		}
	}

	/**
	 * Check whether command is available by checking whether there is online
	 * slave node
	 * 
	 * @return true if there is online slave node
	 * @return false if there is no online slave node
	 */
	private boolean commandAvailable() {
		if (slavesManagement.isEmpty()) {
			System.out
					.println("No Slave Node is online! Command cannot be sent out!");
			return false;
		} else
			return true;
	}

	/**
	 * Terminate specific process on slave node
	 * 
	 * @param command
	 */
	private void terminateProcess(String command) {
		String[] commandArray = command.split(" ");
		String slaveName = commandArray[1];
		long threadID = Long.parseLong(commandArray[2]);
		sendCommand(slaveName, command);
		String feedback = getFeedback(slaveName);
		if (feedback.equals("OK")) {
			System.out.println("Process " + threadID + " on slave " + slaveName
					+ " terminated successfully!");
			processManager.removeProcess(slaveName, threadID);
		}
	}

	/**
	 * Terminate specific slave node
	 * 
	 * @param command
	 */
	private void terminateSlave(String command) {
		String[] commandArray = command.split(" ");
		String slaveName = commandArray[1];
		sendCommand(slaveName, command);
		System.out.println("Removed Slave " + slaveName);
		processManager.removeSlave(slaveName);
	}

	/**
	 * List all the detailed running status of online slave nodes
	 */
	private void listStatus() {
		System.out
				.println("list all the status of the slaves and running process!");
		processManager.listStatus();
	}

	/**
	 * Send migrate process to slave node and recieve the process, then resend
	 * it to the destination node
	 * 
	 * @param command
	 */
	private void migrateProcess(String command) {
		String[] commandArray = command.split(" ");
		String migratedDestSlave = null;
		String migrateSourceSlave = commandArray[1];
		long threadIDSource = Long.parseLong(commandArray[2]);
		if (commandArray.length == 4) {
			migratedDestSlave = commandArray[3];
		} else {
			migratedDestSlave = getAvailableDestSlave().toString();
		}
		sendCommand(migrateSourceSlave, command);
		MigratableProcess migratableProcess = getMigratedProcess(migrateSourceSlave);
		sendMigratableProcess(migratedDestSlave, migratableProcess);
		String feedback = getFeedback(migratedDestSlave);
		int threadID = Integer.parseInt(feedback);
		System.out.println("get the migrated process on " + migratedDestSlave
				+ " rerun threadID: " + threadID);
		processManager.newProcessLaunched(migratedDestSlave, threadID,
				processManager.getProcessName(migrateSourceSlave,
						threadIDSource));
		processManager.removeProcess(migrateSourceSlave, threadIDSource);
	}

	/**
	 * Send the serialized data to destination slave node
	 * 
	 * @param migratedDestSlave
	 *            SlaveNode recieves the migratable process
	 * @param migratableProcess
	 *            MigratableProcess Object
	 */
	private void sendMigratableProcess(String migratedDestSlave,
			MigratableProcess migratableProcess) {
		NodeID slaveNodeID = NodeID.fromString(migratedDestSlave);
		ObjectOutputStream outputStream = getSlaveSocketStream(slaveNodeID);
		try {
			outputStream.writeObject(migratableProcess);
			outputStream.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load balancing. Get the slave node which has the least running process
	 * 
	 * @return NodeID
	 */
	private NodeID getAvailableDestSlave() {
		return processManager.getAvailableSlave();
	}

	/**
	 * launch new process on an auto selected slave node
	 * 
	 * @param command
	 */
	private void launchNewProcess(String command) {
		/* parse the command */
		String[] commandArray = command.split(" ");
		String processName = commandArray[1];
		/* send the command to a specific slave node */
		String destSlave = getAvailableDestSlave().toString();
		sendCommand(destSlave, command);
		String feedback = getFeedback(destSlave);
		if (feedback.equals("Fail")) {
			System.out
					.println("Fail to launch the process, please check arguments!");
			return;
		}
		System.out.println("launch new process on " + destSlave
				+ " threadID is " + feedback);
		int threadID = Integer.parseInt(feedback);
		processManager.newProcessLaunched(destSlave, threadID, processName);
	}

	/**
	 * launch new process on a specific slave node
	 * 
	 * @param command
	 */
	private void targetLaunchNewProcess(String command) {
		/* parse the command */
		String[] commandArray = command.split(" ");
		String destSlave = commandArray[1];
		String processName = commandArray[2];
		/* send the command to a specific slave node */
		sendCommand(destSlave, command);
		String feedback = getFeedback(destSlave);
		if (feedback.equals("Fail")) {
			System.out
					.println("Fail to launch the process, please check arguments!");
			return;
		}
		int threadID = Integer.parseInt(feedback);
		processManager.newProcessLaunched(destSlave, threadID, processName);
		System.out.println("launch new process on " + destSlave
				+ " threadID is " + feedback);
	}

	/**
	 * get the ObjectOutputStream from management
	 * 
	 * @param slaveNodeID
	 * @return ObjectOutputStream
	 */
	private ObjectOutputStream getSlaveSocketStream(NodeID slaveNodeID) {
		return slavesOutputMap.get(slaveNodeID);
	}

	/**
	 * get the serialized migratable process data from socket
	 * 
	 * @param destSlave
	 * @return MigratableProcess
	 */
	private MigratableProcess getMigratedProcess(String destSlave) {
		MigratableProcess feedback = null;
		ObjectInputStream feedBackStream = recieveFeedBackStream(NodeID
				.fromString(destSlave));
		try {
			feedback = (MigratableProcess) feedBackStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return feedback;
	}

	/**
	 * Get feedback from a slave node.
	 * 
	 * @param destSlave
	 * @return String Feedback message sent from slaves
	 */
	private String getFeedback(String destSlave) {
		String feedback = null;
		ObjectInputStream feedBackStream = recieveFeedBackStream(NodeID
				.fromString(destSlave));
		try {
			feedback = (String) feedBackStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return feedback;
	}

	/**
	 * get the ObjectInputStream from management
	 * 
	 * @param slaveNodeID
	 * @return ObjectInputStream
	 */
	private ObjectInputStream recieveFeedBackStream(NodeID slaveNodeID) {
		return slavesInputMap.get(slaveNodeID);
	}

	/**
	 * Send command to a specific slave.
	 * 
	 * @param slaveName
	 * @param command
	 */
	private void sendCommand(String slaveName, String command) {
		NodeID slaveNodeID = NodeID.fromString(slaveName);
		ObjectOutputStream outputStream = getSlaveSocketStream(slaveNodeID);
		try {
			outputStream.writeObject(command);
			outputStream.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stop MasterNode
	 * 
	 */
	private void stop() {
		listen.interrupt();
		terminal.interrupt();
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			RegistrationNode masterNode = new RegistrationNode();
			masterNode.start();
		} else if (args.length == 1) {
			RegistrationNode masterNode = new RegistrationNode(Integer.parseInt(args[0]));
			masterNode.start();
		}
	}
}
