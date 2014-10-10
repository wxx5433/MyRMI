package MyRMIRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import Communication.CommunicationMessage;
import Communication.CommunicationMessage.MessageType;
import Remote.RemoteObjectReference;
import Util.NodeID;

/**
 * 
 * This class is the registry server, it stores the information of where the service is.
 * It is used by the dispatch server to add new service and the client to lookup service.
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
	private Map<String, Map<NodeID, Long>> remoteObjectTable = null;
	
	private String hostName;
	private int portNum;    // the port to listening for incoming requests
	private static final int DEFAULT_PORT_NUM = 11111;
	private static final String HOST_NAME = "128.237.163.210";
	SocketListenThread socketListener;
	Thread listen = null;

	public RegistryServer() {
		this(HOST_NAME, DEFAULT_PORT_NUM);
	}

	public RegistryServer(String hostName, int portNum) {
		this.hostName = hostName;
		this.portNum = portNum;
		socketListener = null;
		listen = null;
		remoteObjectTable = new ConcurrentHashMap<String, Map<NodeID, Long>>();
	}


	/**
	 * add a new service to the registry
	 * @param serviceName the service name to be added
	 * @param ip dispatch server's ip
	 * @param port dispatch server's listening port
	 * @return message to the dispatch server to tell if the request is successful
	 */
	public CommunicationMessage addService(String serviceName, String ip, int port, long objectKey) {
		// new serviceName
		CommunicationMessage replyMessage = null;
		NodeID nodeID = new NodeID(ip, port);
		Map<NodeID, Long> services = null;
		if (!remoteObjectTable.containsKey(serviceName)) {
			services = new ConcurrentHashMap<NodeID, Long>();
			if (objectKey != -1) {
				services.put(nodeID, objectKey);
			} else {
				services.put(nodeID, 0L);
			}
			remoteObjectTable.put(serviceName, services);
			replyMessage = new CommunicationMessage(
					MessageType.ReplyToServer, "Add service Successfully!");
//System.out.println("Add service successfully!");
		} else {    // new service with same name
			// check if this service has already on this server, if it is, then ignore this service.
			services = remoteObjectTable.get(serviceName);
			if (services.containsKey(nodeID) && objectKey == -1) {
				replyMessage = new CommunicationMessage(
						MessageType.ReplyToServer, "Add failed: Duplicate services on the same server");
//System.out.println("Add failed: duplicate service on the same server");
			} else {
				services = new ConcurrentHashMap<NodeID, Long>();
				if (objectKey != -1) {   // already new a object on dispatch server and return the object key
					services.put(nodeID, objectKey);
				} else {
					services.put(nodeID, 0L);    // Initialize the objectKey to 0
				}
				replyMessage = new CommunicationMessage(
						MessageType.ReplyToServer, "Add service Successfully!");
//System.out.println("Add service successfully!");
			}
		}
		return replyMessage;
	}
	
	/**
	 * This method is called by client to look up a service.
	 * If there are several service running on different dispatch node, 
	 * we randomly choose one dispatch node and return the RemoteObjectReference.
	 * @param serviceName the service name to look up
	 * @return null - if the service does not exist. ROR - if the service is found
	 */
	public RemoteObjectReference lookup(String serviceName) {
		RemoteObjectReference ror = null;
		if (!remoteObjectTable.containsKey(serviceName)) {  // no such service
			return null;
		} else {
			// the lists of server running this service
			Map<NodeID, Long> serverList = remoteObjectTable.get(serviceName);
			// randomly choose one to get better load balancing
			Random random = new Random();
			List<NodeID> nodeList = new ArrayList<NodeID>(serverList.keySet());
			if (nodeList.size() == 0) {
				return null;
			}
			NodeID nodeID = nodeList.get(random.nextInt(serverList.size()));
			// increase the objectKey 
			long objectKey = serverList.get(nodeID) + 1;
			serverList.put(nodeID, objectKey);
			ror = new RemoteObjectReference(
					nodeID.getHostName(), nodeID.getPort(), serviceName);
			ror.setObjectKey(objectKey);
		}
		return ror;

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
}
