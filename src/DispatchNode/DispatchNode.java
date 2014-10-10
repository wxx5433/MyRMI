package DispatchNode;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import Communication.CommunicationMessage;
import Communication.CommunicationMessage.MessageType;
import Communication.RMIMessage;
import MyRMIRegistry.MyLocateRegistry;
import MyRMIRegistry.RegistryCommunicator;
import Remote.Remote640;
import Remote.RemoteObjectReference;
import Util.InvokeTask;
import Util.NodeID;
import Util.ServiceID;

/**
 * <code>SlaveNode</code> stores all the running thread information and their
 * related <code>MigratableProcess</code>. It also needs to analyze the command
 * or serialized data from <code>MasterNode</code>
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 */
public class DispatchNode {
	private static final String DEFAULT_DISPATCH_ADDRESS = "128.237.217.63";
	private static final int DEFAULT_SLOT_NUM = 10;
	private static final int DEFAULT_LISTEN_PORT = 11112;

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
				DEFAULT_LISTEN_PORT);
		this.listenPort = DEFAULT_LISTEN_PORT;
	}

	public DispatchNode(String dispatchName, int portNum, int listenPort) {
		dispatchNodeID = new NodeID(dispatchName, portNum);
		this.listenPort = listenPort;
	}

	/**
	 * Start the socket connection thread and the execution thread
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

	private void startNewService(RMIMessage invokeRequest, Socket socket) {
		String serviceName = invokeRequest.getROR().getRemoteInterfaceName();
		Class<?> serviceClass = null;
		try {
			serviceClass = Class.forName("TestService." + serviceName + "."
					+ serviceName + "Impl");
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
		newInvokeRequest(invokeRequest, socket);
	}

	public RemoteObjectReference createRORFromRemote640(InvokeTask invokeTask) {
		String serviceName = invokeTask.getMessage().getROR()
				.getRemoteInterfaceName();
		Remote640 returnObject = (Remote640) invokeTask.getMessage()
				.getReturnValue();
		long newkey = getAvailableKeys(serviceName, returnObject);
		if (newkey != -1) {
			RemoteObjectReference ror = sendNewService(serviceName, newkey);
			ror.setObjectKey(newkey);
			return ror;
		}
		return null;
	}

	private long getAvailableKeys(String serviceName, Remote640 returnObject) {
		for (long i = 0; i <= Long.MAX_VALUE; i++) {
			ServiceID serviceID = new ServiceID(serviceName, i);
			if (!serviceManager.contains(serviceID)) {
				serviceManager.put(serviceID, returnObject);
				return i;
			}
		}
		return -1;
	}

	private RemoteObjectReference sendNewService(String serviceName, long newkey) {
		RegistryCommunicator rc = null;
		try {
			rc = MyLocateRegistry.getRegistry();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rc.rebind(serviceName, dispatchNodeID.getHostName(),
				dispatchNodeID.getPort(), newkey);
		RemoteObjectReference ror = new RemoteObjectReference(
				dispatchNodeID.getHostName(), dispatchNodeID.getPort(),
				serviceName);
		return ror;
	}

	private void addNewServiceToManagement(ServiceID serviceID,
			Remote640 serviceObject) {
		serviceManager.put(serviceID, serviceObject);
	}

	public void newInvokeRequest(RMIMessage invokeRequest, Socket socket) {
		String serviceName = invokeRequest.getROR().getRemoteInterfaceName();
		long key = invokeRequest.getROR().getObjectKey();
		ServiceID serviceID = new ServiceID(serviceName, key);
		if (serviceManager.containsKey(serviceID)) {
			Remote640 serviceObject = serviceManager.get(new ServiceID(
					serviceName, key));
			if (serviceObject != null) {
				invokeRequestQueue.add(new InvokeTask(invokeRequest, socket,
						serviceObject));
			}
		} else {
			startNewService(invokeRequest, socket);
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

	public RemoteObjectReference getROR(InvokeTask invokeTask) {
		ServiceID serviceID = checkInMap(invokeTask);
		if (serviceID != null) {
			return createRORFromServiceID(serviceID);
		} else {
			return createRORFromRemote640(invokeTask);
		}
	}

	private RemoteObjectReference createRORFromServiceID(ServiceID serviceID) {
		RemoteObjectReference ror = new RemoteObjectReference(
				dispatchNodeID.getHostName(), dispatchNodeID.getPort(),
				serviceID.getSericeName());
		ror.setObjectKey(serviceID.getKey());
		return ror;
	}

	private ServiceID checkInMap(InvokeTask invokeTask) {
		Remote640 object = (Remote640) invokeTask.getMessage().getReturnValue();
		for (Map.Entry<ServiceID, Remote640> m : serviceManager.entrySet()) {
			if (m.getValue().equals(object)) {
				return m.getKey();
			}
		}
		return null;
	}
}
