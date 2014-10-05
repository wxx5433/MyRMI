package Communication;

import java.io.Serializable;
import java.lang.reflect.Method;

import Remote.Remote640;

public class RMIMessage implements Serializable {
	private static final long serialVersionUID = 5927860882914402321L;

	/**
	 * The method name to invoke. 
	 * Look up the table to find the object
	 */
	private String remoteInterfaceName;
	private long objectKey;
	private String methodName;
	private Object[] args;
	private Object returnValue;
	private Exception exception;
	
	
	public RMIMessage(String riName, String methodName, Object[] args) {
		this(riName, 0L, methodName, args);
	}

	public RMIMessage(String riName, long objectKey, String methodName, Object[] args) {
		setRemoteInterfaceName(riName);
		setObjectKey(objectKey);
		setMethodName(methodName);
		setArgs(args);
		setReturnValue(null);
		setException(null);
	}

	/**
	 * This method should be invoked by the server.
	 * Then the server will call the specific method locally,
	 * and marshaled the return value into RMIMessage.
	 * @param object 
	 * @throws Exception
	 */
	public void call(Object object) throws Exception {
		if (object == null || methodName == null) {
			throw new Exception("bad invoke");
		}
		
		Class<?>[] argsTypes = null;
		int argsNum = args.length;
		// have arguments, then parse the types
		if (argsNum > 0) {
			argsTypes = new Class<?>[argsNum];
			for (int i = 0; i < argsNum; ++i) {
				if (args[i] instanceof Remote640) {
					// Must put the desired interface at first place
					argsTypes[i] = args[i].getClass().getInterfaces()[0];
				} else {
					argsTypes[i] = args[i].getClass();
				}
System.out.println("argsType: " + argsTypes[i]);
			}
		}
		
		// invoke the method at server
		Method method = object.getClass().getMethod(methodName, argsTypes);
		returnValue = method.invoke(object, args);
	}
	
	
	
	/**
	 * @return the remoteInterfaceName
	 */
	public String getRemoteInterfaceName() {
		return remoteInterfaceName;
	}

	/**
	 * @param remoteInterfaceName the remoteInterfaceName to set
	 */
	public void setRemoteInterfaceName(String remoteInterfaceName) {
		this.remoteInterfaceName = remoteInterfaceName;
	}

	/**
	 * @return the objectKey
	 */
	public long getObjectKey() {
		return objectKey;
	}

	/**
	 * @param objectKey the objectKey to set
	 */
	public void setObjectKey(long objectKey) {
		this.objectKey = objectKey;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
}
