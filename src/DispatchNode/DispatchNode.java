package DispatchNode;

import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.Map.Entry;
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
	private static final String DEFAULT_REGISTRATION_ADDRESS = "localhost";
	private static final int DEFAULT_REGISTRATION_PORT = 10000;
	private static final String DEFAULT_DISPATCH_ADDRESS = "localhost";
	private static final int DEFAULT_DISPATCH_PORT = 8888;
	private static final int DEFAULT_SLOT_NUM = 4;
	private static final int DEFAULT_LISTEN_PORT = 9999;

	private NodeID dispatchNodeID;
	private NodeID registrationNodeID;
	private int listenPort;
	private ConcurrentHashMap<ServiceID, Remote640> serviceManager = new ConcurrentHashMap<ServiceID, Remote640>();
	public BlockingDeque<InvokeTask> invokeRequestQueue = new LinkedBlockingDeque<InvokeTask>();

	private Thread executor = null;
	private ExecutionExecutor executionExecutor = null;
	private Thread dispatchServiceSocketThread = null;
	private DispatchServiceSocketThread dispatchServiceSocket = null;
	private Thread dispatchListenSocketThread = null;
	private DispatchListenSocketThread dispatchListenSocket = null;

	public DispatchNode() {
		dispatchNodeID = new NodeID(DEFAULT_DISPATCH_ADDRESS,
				DEFAULT_DISPATCH_PORT);
		registrationNodeID = new NodeID(DEFAULT_REGISTRATION_ADDRESS,
				DEFAULT_REGISTRATION_PORT);
		this.listenPort = DEFAULT_LISTEN_PORT;
	}

	public DispatchNode(String dispatchName, int portNum,
			String registrationAddress, int registrationPort, int listenPort) {
		dispatchNodeID = new NodeID(dispatchName, portNum);
		registrationNodeID = new NodeID(registrationAddress, registrationPort);
		this.listenPort = listenPort;
	}

	/**
	 * Start the socket connection thread
	 * 
	 */
	public void start() {
		dispatchServiceSocket = new DispatchServiceSocketThread(this,
				registrationNodeID);
		dispatchListenSocket = new DispatchListenSocketThread(this, listenPort);
		executionExecutor = new ExecutionExecutor(this);
		dispatchServiceSocketThread = new Thread(dispatchServiceSocket);
		dispatchListenSocketThread = new Thread(dispatchListenSocket);
		executor = new Thread(executionExecutor);
		dispatchServiceSocketThread.start();
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
	private void NewServiceRequest(String command) {
		System.out.println("recived command is: " + command);
		String commandBak = command;
		String[] commandArray = commandBak.split(" ");
		String serviceName = commandArray[1];
		System.out.println("Service Name is: " + serviceName);
		String[] args = null;
		int blankPos = command.indexOf(" ");
		blankPos = command.indexOf(" ", blankPos + 1);
		if (blankPos != -1) {
			String argsStr = command.substring(blankPos + 1, command.length());
			args = argsStr.split(" ");
		}
		startNewService(serviceName, args);

	}

	private void startNewService(String serviceName, String[] args) {
		Class<?> serviceClass = null;
		try {
			serviceClass = Class.forName("RemoteService." + serviceName);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		Constructor<?> constructor = null;
		Remote640 serviceObject = null;
		if (args.length != 0) {
			try {
				constructor = serviceClass.getConstructor(String[].class);
			} catch (NoSuchMethodException | SecurityException e1) {
				e1.printStackTrace();
			}
			try {
				serviceObject = (Remote640) constructor
						.newInstance(new Object[] { args });
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				serviceObject = (Remote640) serviceClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		ServiceID serviceID = new ServiceID(serviceName, 0);
		addNewServiceToManagement(serviceID, serviceObject);
		CommunicationMessage message = new CommunicationMessage(
				MessageType.NewService, serviceName);
		dispatchServiceSocket.setNewMessage(message);
		dispatchServiceSocketThread.notify();
	}

	private void addNewServiceToManagement(ServiceID serviceID,
			Remote640 serviceObject) {
		serviceManager.put(serviceID, serviceObject);
	}

	/**
	 * delete a specific service
	 * 
	 * @param command
	 */
	private void deleteService(String command) {
		String[] commandArray = command.split(" ");
		String serviceID = commandArray[1];
		CommunicationMessage message = new CommunicationMessage(
				MessageType.DelService, serviceID);
		dispatchServiceSocket.setNewMessage(message);
		dispatchServiceSocketThread.notify();
		serviceManager.remove(ServiceID.fromString(serviceID));
	}

	private void listServices() {
		for (Entry<ServiceID, Remote640> entry : serviceManager.entrySet()) {
			System.out.println(entry.getKey().toString() + "\t"
					+ entry.getValue().toString());
		}
	}

	public void parseCommand(String command) {
		System.out.println(command + " recieved!");
		if (command.startsWith("start")) {
			NewServiceRequest(command);
		} else if (command.startsWith("list")) {
			listServices();
		} else if (command.startsWith("delete")) {
			deleteService(command);
		} else if (command.startsWith("exit")) {
			stop();
		}
	}

	public void newInvokeRequest(RMIMessage invokeRequest,
			ObjectOutputStream outputStream) {
		String serviceName = invokeRequest.getROR().getRemoteInterfaceName();
		long key = invokeRequest.getROR().getObjectKey();
		Remote640 serviceObject = serviceManager.get(new ServiceID(serviceName,
				key));
		if (serviceObject != null) {
			invokeRequestQueue.add(new InvokeTask(invokeRequest, outputStream,
					serviceObject));
		}
	}

	public void recieveFeedBack(CommunicationMessage message) {
		if (message.getMessageType() == MessageType.ReplyServiceID) {
			ServiceID serviceID = ServiceID.fromString(message.getMessage());
			updateServiceManage(serviceID);
		} else if (message.getMessageType() == MessageType.ReplyToServer) {
			System.out.println(message.getMessage());
		}
	}

	private void updateServiceManage(ServiceID serviceID) {
		String serviceName = serviceID.getSericeName();
		ServiceID oldServiceID = new ServiceID(serviceName, 0);
		Remote640 serviceObject = serviceManager.get(oldServiceID);
		serviceManager.remove(oldServiceID);
		serviceManager.put(serviceID, serviceObject);
	}

	public int getSlotNumber() {
		return DEFAULT_SLOT_NUM;
	}

	/**
	 * Stop dispatch node
	 */
	private void stop() {
		executionExecutor.terminate();
		dispatchListenSocket.terminate();
		dispatchServiceSocket.terminate();
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			DispatchNode dispatchNode = new DispatchNode();
			dispatchNode.start();
		} else if (args.length == 5) {
			DispatchNode dispatchNode = new DispatchNode(args[0],
					Integer.parseInt(args[1]), args[2],
					Integer.parseInt(args[3]), Integer.parseInt(args[4]));
			dispatchNode.start();
		}
	}

}
