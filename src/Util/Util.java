package Util;

import java.lang.reflect.Proxy;

import Remote.Remote640;
import Remote.RemoteObjectReference;

public abstract class Util {
	/**
	 * Create a proxy here. 
	 * @param impl  The object that directly or indirectly implements Remote640 interface
	 * @param clientRef
	 * @return
	 */
	public static Remote640 createProxy(Object impl, RemoteObjectReference clientRef) {
		RemoteObjectInvocationHandler handler = new RemoteObjectInvocationHandler(impl);
		Remote640 proxy = (Remote640) Proxy.newProxyInstance(impl.getClass().getClassLoader(),
				impl.getClass().getInterfaces(), handler);
		return proxy;
	}
}
