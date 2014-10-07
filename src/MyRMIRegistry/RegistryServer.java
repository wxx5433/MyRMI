package MyRMIRegistry;

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
import Stub.Stub;
import Utils.NodeID;
import Utils.ServiceID;

/**
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 *
 */
public class RegistryServer {
	/**
	 * The table to store remoteObjectReference
	 * 1st map:  Key: serviceName,  Value: 2nd map
	 * 2nd map:  Key: server running this service(may exist several servers), Value: ObjectKey 
	 */
	private Map<String, Map<NodeID, Integer>> remoteObjectTable = null;
	
	private int portNum;    // the port to listening for incoming requests
	private static final int DEFAULT_PORT_NUM = 11111;
	SocketListenThread socketListener;
	Thread listen = null;

	public RegistryServer() {
		this(DEFAULT_PORT_NUM);
	}

	public RegistryServer(int portNum) {
		this.portNum = portNum;
		socketListener = null;
		listen = null;
		remoteObjectTable = new ConcurrentHashMap<String, Map<NodeID, Integer>>();
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

	public static void main(String[] args) throws Exception {
		RegistryServer registry = new RegistryServer();
		// start listening for incoming request
		registry.start();
	}

	/**
	 * add a new service to the registry
	 * @throws Exception 
	 */
	public CommunicationMessage addService(String serviceName, String ip, int port) {
		// new serviceName
		CommunicationMessage replyMessage = null;
		NodeID nodeID = new NodeID(ip, port);
		Map<NodeID, Integer> services = null;
		if (!remoteObjectTable.containsKey(serviceName)) {
			services = new ConcurrentHashMap<NodeID, Integer>();
			services.put(nodeID, 0);
			remoteObjectTable.put(serviceName, services);
			replyMessage = new CommunicationMessage(
					MessageType.ReplyToServer, "Add service Successfully!");
System.out.println("Add service successfully!");
		} else {    // new service with same name
			// check if this service has already on this server, if it is, then ignore this service.
			services = remoteObjectTable.get(serviceName);
			if (services.containsKey(nodeID)) {
				replyMessage = new CommunicationMessage(
						MessageType.ReplyToServer, "Add failed: Duplicate services on the same server");
System.out.println("Add failed: duplicate service on the same server");
			} else {
				services = new ConcurrentHashMap<NodeID, Integer>();
				services.put(nodeID, 0);    // Initialize the objectKey to 0
				replyMessage = new CommunicationMessage(
						MessageType.ReplyToServer, "Add service Successfully!");
System.out.println("Add service successfully!");
			}
		}
		return replyMessage;
	}
	
	public RemoteObjectReference lookup(String serviceName) {
		RemoteObjectReference ror = null;
		if (!remoteObjectTable.containsKey(serviceName)) {  // no such service
			return null;
		} else {
			// the lists of server running this service
			Map<NodeID, Integer> serverList = remoteObjectTable.get(serviceName);
			// randomly choose one to get better load balancing
			Random random = new Random();
			List<NodeID> nodeList = new ArrayList<NodeID>(serverList.size());
			NodeID nodeID = nodeList.get(random.nextInt(serverList.size()));
			// increase the objectKey 
			int objectKey = serverList.get(nodeID) + 1;
			serverList.put(nodeID, objectKey);
			ror = new RemoteObjectReference(
					nodeID.getHostName(), nodeID.getPort(), serviceName);
			ror.setObjectKey(objectKey);
		}
		return ror;

	}

}
