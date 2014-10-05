package Util;

import java.lang.reflect.Proxy;

import Remote.Remote640;

public abstract class Util {
	/**
	 * Create a proxy here.
	 * When the client call a remote method,
	 * the proxy sends the remote invocation message to the server. 
	 * @param stub  The stub object that directly or indirectly implements Remote640 interface
	 * @return the proxy
	 */
	public static Remote640 createProxy(Stub stub) {
		RemoteObjectInvocationHandler handler = new RemoteObjectInvocationHandler(stub);
		Remote640 proxy = (Remote640) Proxy.newProxyInstance(stub.getClass().getClassLoader(),
				stub.getClass().getInterfaces(), handler);
		return proxy;
	}
}
