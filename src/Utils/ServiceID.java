package Utils;

/**
 * Just put the hostname/IP address and port number all in And override some
 * functions to make this class can be the key of map
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 */
public class ServiceID {
	private String serviceName;
	private long key;
	private final String nodeID;
	private final int hash;

	public ServiceID(String serviceName, long key) {
		this.serviceName = serviceName;
		this.key = key;
		nodeID = serviceName + ":" + key;
		hash = nodeID.hashCode();
	}

	public String getSericeName() {
		return serviceName;
	}

	public long getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return nodeID;
	}

	public static ServiceID fromString(String nodeIdStr) {
		int divideIndex = nodeIdStr.indexOf(":");
		String host = nodeIdStr.substring(0, divideIndex);
		long port = Long.parseLong(nodeIdStr.substring(divideIndex + 1));
		return new ServiceID(host, port);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof ServiceID) {
			ServiceID anotherServiceID = (ServiceID) anObject;
			if (key == anotherServiceID.key
					&& serviceName.equals(anotherServiceID.serviceName))
				return true;
		}
		return false;
	}

}
