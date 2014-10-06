package RegistrationNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import Communication.CommunicationMessage;
import Communication.CommunicationMessage.MessageType;
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
	 * store unique serviceId, which refers to a remote object service on the server.
	 */
	private Map<String, Map<ServiceID, RemoteObjectReference>> remoteObjectTable = null;
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
		remoteObjectTable = new ConcurrentHashMap<String, Map<ServiceID, RemoteObjectReference>>();
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
		healthTable.put(nodeID, System.currentTimeMillis());
System.out.println("New server online!");
		// reply to the server
		CommunicationMessage message = new CommunicationMessage(
				MessageType.ReplyToDis, "New server successfully connect!");
		try {
			out.writeObject(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * bind a new service on the registry
	 * @throws Exception 
	 */
	public void rebind(String message, ObjectOutputStream out) throws Exception {
		// message format: serviceName | IP | port
		String[] fields = message.split("|");
		if (fields.length != 3) {
				throw new Exception("bad format message!");
		}
		String serviceName = fields[0];
		String IP = fields[1];
		int port = Integer.parseInt(fields[2]);
		// add a timestamp to uniquely identify a service
		ServiceID serviceID = new ServiceID(serviceName, System.currentTimeMillis());
		RemoteObjectReference ror = new RemoteObjectReference(IP, port, serviceName);
		// new serviceName
		if (!remoteObjectTable.containsKey(serviceName)) {
			Map<ServiceID, RemoteObjectReference> services = 
					new ConcurrentHashMap<ServiceID, RemoteObjectReference>();
			services.put(serviceID, ror);
			remoteObjectTable.put(serviceName, services);
		} else {    // new service with same name
			Map<ServiceID, RemoteObjectReference> services = 
					remoteObjectTable.get(serviceName);
			services.put(serviceID, ror);
		}
		
		// send feedback to the server
		CommunicationMessage replyMessage = new CommunicationMessage(
				MessageType.ReplyToDis, "New service added sucessfully!");
		out.writeObject(replyMessage);
	}
	
	/**
	 * remove a service from the remoteObjectTable
	 * @param serviceID
	 */
	public void deleteService(ServiceID serviceID, ObjectOutputStream out) {
		remoteObjectTable.remove(serviceID);
		Map<ServiceID, RemoteObjectReference> serviceList 
				= remoteObjectTable.get(serviceID);
		CommunicationMessage message = null;
		if (serviceList.containsKey(serviceID)) {
			// delete the service from remoteObjectTable
			remoteObjectTable.get(serviceID.getSericeName()).remove(serviceID);
			message = new CommunicationMessage(
					MessageType.ReplyToDis, "Successfully delete service!");
System.out.println("Remove service successfully!");
		} else {
			message = new CommunicationMessage(
					MessageType.ReplyToDis, "Delete service failed: no such service!");
System.out.println("Remove service failed!");
		}

		// send reply to the server
		try {
			out.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// arbitrarily send one service with the same serviceName to the client
	public RemoteObjectReference lookup(String serviceName) {
		/* This is the serviceList that running the service with the serviceName,
		 * The same service may be launched several times.
		 */
		Map<ServiceID, RemoteObjectReference> serviceList = remoteObjectTable.get(serviceName);
		// randomly choose one to get better load balancing
		Random random = new Random();
		List<ServiceID> services = new ArrayList<ServiceID>(serviceList.keySet());
		ServiceID service = services.get(random.nextInt(services.size()));
		RemoteObjectReference ror = serviceList.get(service);
// some problem here!
		return ror;
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
