package Util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Socket;

import Communication.RMIMessage;
import Exception.MyRemoteException;
import Remote.RemoteObjectReference;
import Stub.Stub;

/**
 * This class is used by the proxy to send remote object's method invocation 
 * directly to remote server and get return value to the client.
 * The process is transparent to the client.
 * @author Xiaoxiang Wu (xiaoxiaw)
 * @author Ye Zhou (zhouye)
 */
public class RemoteObjectInvocationHandler implements InvocationHandler {
	private Stub stub = null;
	
	public RemoteObjectInvocationHandler(Stub stub) {
		this.stub = stub;
	}

	/**
	 * send the remote object's method invocation to remote server.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable, MyRemoteException {
		// send requests.
		RemoteObjectReference ror = stub.getRemoteObjectReference();
		RMIMessage sendMessage = new RMIMessage(ror, method.getName(), args);
		Socket socket = new Socket(ror.getHostIP(), ror.getPort());
		Util.sendRemoteCallRequest(socket, sendMessage);
		
		RMIMessage responseMessage = null;
		// receive response.
		try {
			responseMessage = Util.getRemoteCallResponse(socket);
		} catch (MyRemoteException e) {
			throw e;
		}
		
		Object returnValue = responseMessage.getReturnValue();
		socket.close();
		
		return returnValue;
	}
	
	public Stub getStub() {
		return stub;
	}
}
