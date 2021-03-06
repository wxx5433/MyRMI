package Util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

import Communication.RMIMessage;
import Exception.MyRemoteException;
import Remote.Remote640;
import Stub.Stub;

/**
 * This is a helping class which is used to create proxy for a remote object.
 * And can send RMIMessage to and receive RMIMessage from the dispatch server.
 * 
 * @author Xiaoxiang Wu (xiaoxiaw)
 * @author Ye Zhou (zhouye)
 */
public abstract class ProxyMessenger {
	/**
	 * Create a proxy here. When the client call a remote method, the proxy
	 * sends the remote invocation message to the server.
	 * 
	 * @param stub
	 *            The stub object that directly or indirectly implements
	 *            Remote640 interface
	 * @return the proxy
	 */
	public static Remote640 createProxy(Stub stub) {
		RemoteObjectInvocationHandler handler = new RemoteObjectInvocationHandler(
				stub);
		Remote640 proxy = (Remote640) Proxy.newProxyInstance(stub.getClass()
				.getClassLoader(), stub.getClass().getInterfaces(), handler);
		return proxy;
	}

	/**
	 * Send a remote call to the server running the service.
	 * 
	 * @param socket
	 *            socket connection with the server
	 * @param message
	 *            the message to send
	 */
	public static void sendRemoteCallRequest(Socket socket, RMIMessage message) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			out.writeObject(message);
			// out.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * receive the result of a remote call from the server.
	 * 
	 * @param socket
	 *            socket connection with the server
	 * @return return value from the remote function call.
	 * @throws MyRemoteException
	 */
	public static RMIMessage getRemoteCallResponse(Socket socket)
			throws MyRemoteException {
		RMIMessage response = null;
		try {
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			response = (RMIMessage) in.readObject();
			if (response.getException() != null) {
				throw new MyRemoteException(response.getException());
			}
			// in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
}
