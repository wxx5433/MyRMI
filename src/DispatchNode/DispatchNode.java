package DispatchNode;

import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import Communication.CommunicationMessage;
import Communication.CommunicationMessage.MessageType;
import Communication.RMIMessage;
import Remote.Remote640;
import Utils.InvokeTask;
import Utils.NodeID;
import Utils.ServiceID;

/**
 * <code>SlaveNode</code> stores all the running thread information and their
 * related <code>MigratableProcess</code>. It also needs to analyze the command
 * or serialized data from <code>MasterNode</code>
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 */
public class DispatchNode {
	private static final String DEFAULT_DISPATCH_ADDRESS = "localhost";
	private static final int DEFAULT_DISPATCH_PORT = 8888;
	private static final int DEFAULT_SLOT_NUM = 10;
	private static final int DEFAULT_LISTEN_PORT = 9999;

	private NodeID dispatchNodeID;
	private int listenPort;
	private ConcurrentHashMap<ServiceID, Remote640> serviceManager = new ConcurrentHashMap<ServiceID, Remote640>();
	public BlockingDeque<InvokeTask> invokeRequestQueue = new LinkedBlockingDeque<InvokeTask>();

	private Thread executor = null;
	private ExecutionExecutor executionExecutor = null;
	private Thread dispatchListenSocketThread = null;
	private DispatchListenSocketThread dispatchListenSocket = null;

	public DispatchNode() {
		dispatchNodeID = new NodeID(DEFAULT_DISPATCH_ADDRESS,
				DEFAULT_DISPATCH_PORT);
		this.listenPort = DEFAULT_LISTEN_PORT;
	}

	public DispatchNode(String dispatchName, int portNum, int listenPort) {
		dispatchNodeID = new NodeID(dispatchName, portNum);
		this.listenPort = listenPort;
	}

	/**
	 * Start the socket connection thread
	 * 
	 */
	public void start() {
		dispatchListenSocket = new DispatchListenSocketThread(this, listenPort);
		executionExecutor = new ExecutionExecutor(this);
		dispatchListenSocketThread = new Thread(dispatchListenSocket);
		executor = new Thread(executionExecutor);
		dispatchListenSocketThread.start();
		executor.start();
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
	 * Start new service
	 * 
	 * @param command
	 */

	private void startNewService(RMIMessage invokeRequest,
			ObjectOutputStream outputStream) {
		String serviceName = invokeRequest.getROR().getRemoteInterfaceName();
		Class<?> serviceClass = null;
		try {
			serviceClass = Class.forName("RemoteService." + serviceName);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		Remote640 serviceObject = null;
		try {
			serviceObject = (Remote640) serviceClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		long key = invokeRequest.getROR().getObjectKey();
		ServiceID serviceID = new ServiceID(serviceName, key);
		addNewServiceToManagement(serviceID, serviceObject);
		newInvokeRequest(invokeRequest, outputStream);
	}

	private void addNewServiceToManagement(ServiceID serviceID,
			Remote640 serviceObject) {
		serviceManager.put(serviceID, serviceObject);
	}

	public void newInvokeRequest(RMIMessage invokeRequest,
			ObjectOutputStream outputStream) {
		String serviceName = invokeRequest.getROR().getRemoteInterfaceName();
		long key = invokeRequest.getROR().getObjectKey();
		ServiceID serviceID = new ServiceID(serviceName, key);
		if (serviceManager.containsKey(serviceID)) {
			Remote640 serviceObject = serviceManager.get(new ServiceID(
					serviceName, key));
			if (serviceObject != null) {
				invokeRequestQueue.add(new InvokeTask(invokeRequest,
						outputStream, serviceObject));
			}
		} else {
			startNewService(invokeRequest, outputStream);
		}
	}

	public void recieveFeedBack(CommunicationMessage message) {
		if (message.getMessageType() == MessageType.ReplyToServer) {
			System.out.println(message.getMessage());
		}
	}

	public int getSlotNumber() {
		return DEFAULT_SLOT_NUM;
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			DispatchNode dispatchNode = new DispatchNode();
			dispatchNode.start();
		} else if (args.length == 3) {
			DispatchNode dispatchNode = new DispatchNode(args[0],
					Integer.parseInt(args[1]), Integer.parseInt(args[4]));
			dispatchNode.start();
		}
	}

}
