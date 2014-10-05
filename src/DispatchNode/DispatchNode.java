package DispatchNode;

import java.util.HashMap;

import Utils.NodeID;

/**
 * <code>SlaveNode</code> stores all the running thread information and their
 * related <code>MigratableProcess</code>. It also needs to analyze the command
 * or serialized data from <code>MasterNode</code>
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 */
public class DispatchNode {
	private static final String DEFAULT_REGISTRATION_ADDRESS = "localhost";
	private static final int DEFAULT_REGISTRATION_PORT = 10000;
	private static final String DEFAULT_DISPATCH_ADDRESS = "localhost";
	private static final int DEFAULT_DISPATCH_PORT = 8888;
	private static final int DEFAULT_SLOT_NUM = 4;
	private Thread executor = null;
	private ExecutionExecutor executionExecutor = null;
	private NodeID dispatchNodeID;
	private NodeID registrationNodeID;
	private Thread dispatchServiceSocketThread = null;
	private DispatchServiceSocketThread dispatchServiceSocket = null;
	private Thread dispatchListenSocketThread = null;
	private DispatchListenSocketThread dispatchListenSocket = null;
	private HashMap<String, Object> serviceManager = new HashMap<String, Object>();

	public DispatchNode() {
		dispatchNodeID = new NodeID(DEFAULT_DISPATCH_ADDRESS,
				DEFAULT_DISPATCH_PORT);
		registrationNodeID = new NodeID(DEFAULT_REGISTRATION_ADDRESS,
				DEFAULT_REGISTRATION_PORT);
	}

	public DispatchNode(String slaveName, int portNum, String masterAddress,
			int masterPort) {
		dispatchNodeID = new NodeID(slaveName, portNum);
		registrationNodeID = new NodeID(masterAddress, masterPort);
	}

	/**
	 * Start the socket connection thread
	 * 
	 */
	public void start() {
		slaveSocket = new DispatchServiceSocketThread(this, registrationNodeID);
		socketThread = new Thread(slaveSocket);
		socketThread.start();
	}

	/**
	 * get the slave's name
	 * 
	 * @return the slave Name including hostname, IP address and port
	 */
	public String getDispatchName() {
		return dispatchNodeID.toString();
	}

	/**
	 * Start new process and return its threadID
	 * 
	 * @param processName
	 * @param args
	 * @return threadID
	 */
	private String startNewService(String serviceName, String[] args) {
		return serviceName;

	}

	/**
	 * terminate a specific thread
	 * 
	 * @param command
	 */
	private void deleteService(long serviceKey, String serviceName) {
	}

	public int getSlotNumber() {
		return DEFAULT_SLOT_NUM;
	}

	/**
	 * Stop slave node
	 */
	private void stop() {
		slaveSocket.terminate();
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			DispatchNode slaveNode = new DispatchNode();
			slaveNode.start();
		} else if (args.length == 4) {
			DispatchNode slaveNode = new DispatchNode(args[0],
					Integer.parseInt(args[1]), args[2],
					Integer.parseInt(args[3]));
			slaveNode.start();
		}
	}

}
