package Communication;

import java.io.Serializable;
import java.lang.reflect.Method;

import Remote.Remote640;
import Remote.RemoteObjectReference;

public class RMIMessage implements Serializable {
	private static final long serialVersionUID = 5927860882914402321L;

	/**
	 * The method name to invoke. 
	 * Look up the table to find the object
	 */
	private String methodName;
	private Object[] args;
	private Object returnValue;
	private Exception exception;
	
	
	public RMIMessage(String methodName, Object[] args) {
		this.methodName = methodName;
		this.args = args;
		returnValue = null;
		exception = null;
	}
	
	public void invoke(Object object) throws Exception {
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
			}
		}
		
		Method method = object.getClass().getMethod(methodName, argsTypes);
		returnValue = method.invoke(object, argsTypes);
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
