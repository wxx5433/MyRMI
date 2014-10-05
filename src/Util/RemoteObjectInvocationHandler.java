package Util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Xiaoxiang Wu (xiaoxiaw)
 * @author Ye Zhou (zhouye)
 */
public class RemoteObjectInvocationHandler implements InvocationHandler {
	private Object target = null;
	
	public RemoteObjectInvocationHandler(Object target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// send requests.
		
		// receive response.
		
		return null;
	}
	
	
}
