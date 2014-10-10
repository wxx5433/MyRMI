package Remote;

import java.io.Serializable;

import Stub.Stub;
import Util.ProxyMessenger;

/**
 * This class stores information for a remote object, including ip and port of
 * the dispatch server where the object is, the objectKey which uniquely
 * identify an object on the dispatch server, and the service's name.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(zhouye)
 */
public class RemoteObjectReference implements Serializable {
	private static final long serialVersionUID = 3944776457281619555L;

	/**
	 * The dispatch node's ip where the object is located
	 */
	private String hostIP;
	/**
	 * The dispatch node's listening port where the object is located
	 */
	private int port;
	/**
	 * uniquely identify an object on one dispatch server
	 */
	private long objectKey;
	/**
	 * The service's name
	 */
	private String remoteInterfaceName;

	public RemoteObjectReference(String ip, int port, String riname) {
		setHostIP(ip);
		setPort(port);
		setRemoteInterfaceName(riname);
		setObjectKey(-1);
	}

	/**
	 * Localize the <code>RemoteObjectReference</code>, return the proxy for the
	 * remote object. Then the client can use the proxy to invoke remote method
	 * without knowing the details of remote invocation. The proxy sends the
	 * remote invocation message to the server.
	 * 
	 * @return a proxy
	 */
	public Remote640 localize() {
		String stubClassName = "TestService." + remoteInterfaceName + "."
				+ remoteInterfaceName + "_stub";
		Remote640 proxy = null;

		try {
			Class<?> stubClass = Class.forName(stubClassName);
			Stub stub = (Stub) stubClass.newInstance();
			stub.setRemoteObjectReference(this);
			proxy = ProxyMessenger.createProxy(stub);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return proxy;
	}

	public String getHostIP() {
		return hostIP;
	}

	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getObjectKey() {
		return objectKey;
	}

	public void setObjectKey(long objectKey) {
		this.objectKey = objectKey;
	}

	public String getRemoteInterfaceName() {
		return remoteInterfaceName;
	}

	public void setRemoteInterfaceName(String remoteInterfaceName) {
		this.remoteInterfaceName = remoteInterfaceName;
	}
}
