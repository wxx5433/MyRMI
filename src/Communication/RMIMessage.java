package Communication;

import java.io.Serializable;

public class RMIMessage implements Serializable {
	private static final long serialVersionUID = 5927860882914402321L;

	/**
	 * The method name to invoke.
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
	
	public void invoke(Object object) {
	}
}
