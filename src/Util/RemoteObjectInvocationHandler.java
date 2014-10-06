package Util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Socket;

import Communication.RMIMessage;
import Remote.RemoteObjectReference;
import Stub.Stub;

/**
 * @author Xiaoxiang Wu (xiaoxiaw)
 * @author Ye Zhou (zhouye)
 */
public class RemoteObjectInvocationHandler implements InvocationHandler {
	private Stub stub = null;
	
	public RemoteObjectInvocationHandler(Stub stub) {
		this.stub = stub;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// send requests.
		RemoteObjectReference ror = stub.getRemoteObjectReference();
		RMIMessage sendMessage = new RMIMessage(ror, method.getName(), args);
		Socket socket = new Socket(ror.getHostIP(), ror.getPort());
		Util.sendRemoteCallRequest(socket, sendMessage);

		// receive response.
		RMIMessage responseMessage = Util.getRemoteCallResponse(socket);
		
		return responseMessage;
	}
	
	
}
