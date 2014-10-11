package Communication;

import java.io.Serializable;
import java.lang.reflect.Method;

import Remote.Remote640;
import Remote.RemoteObjectReference;

/**
 * The class is used between the client and the dispatch server. The proxy will
 * marshal clients' remote method invocation into <code>RMIMessage</code>, and
 * then send it to the dispatch server. The dispatch server then unmarshal the
 * <code>RMIMessage</code> and call the method locally, and put the return value
 * into <code>RMIMessage</code> and send it back to the client.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou (yezhou)
 */
public class RMIMessage implements Serializable {
	private static final long serialVersionUID = 5927860882914402321L;

	/**
	 * The method name to invoke. Look up the table to find the object
	 */
	private String methodName;
	/**
	 * The remote object reference who call this method
	 */
	private RemoteObjectReference ROR;
	private Object[] args;
	private Object returnValue;
	private boolean returnROR;
	private Exception exception;

	public RMIMessage(RemoteObjectReference ror, String methodName,
			Object[] args) {
		setROR(ror);
		setMethodName(methodName);
		setArgs(args);
		setReturnValue(null);
		setException(null);
		setReturnROR(false);
	}

	/**
	 * This method should be invoked by the server. Then the server will call
	 * the specific method locally, and marshaled the return value into
	 * RMIMessage.
	 * 
	 * @param object
	 * @throws Exception
	 */
	public void call(Object object) throws Exception {
		if (object == null || methodName == null) {
			throw new Exception("bad invoke");
		}

		Class<?>[] argsTypes = null;
		// have arguments, then parse the types
		if (args != null) {
			int argsNum = args.length;
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

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public RemoteObjectReference getROR() {
		return ROR;
	}

	public void setROR(RemoteObjectReference rOR) {
		ROR = rOR;
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

	public boolean isReturnROR() {
		return returnROR;
	}

	public void setReturnROR(boolean returnROR) {
		this.returnROR = returnROR;
	}
}
