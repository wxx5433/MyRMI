package RegistrationNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Communication.CommunicationMessage;
import DispatchNode.DisTerminalThread;
import Remote.RemoteObjectReference;
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
	 * If one new slave is connected to the registry
	 * 
	 * @param slaveName
	 * @param socket
	 * @param out
	 * @param input
	 */
	public void newServerOnline(NodeID nodeID, Socket socket, 
			ObjectInputStream in, ObjectOutputStream out) {
		socketTable.put(nodeID, socket);
		outputStreamTable.put(socket, out);
		inputStreamTable.put(socket, in);
		System.out.println("New server online!");
	}

	/**
	 * bind a new service on the registry
	 */
	public void rebind() {
		
	}
	
	
	public RemoteObjectReference lookup() {
		
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
