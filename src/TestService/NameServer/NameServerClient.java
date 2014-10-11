package TestService.NameServer;

import java.io.IOException;
import java.lang.reflect.Proxy;

import Exception.MyRemoteException;
import MyRMIRegistry.MyLocateRegistry;
import MyRMIRegistry.RegistryCommunicator;
import Remote.RemoteObjectReference;
import Util.RemoteObjectInvocationHandler;

public class NameServerClient {

	public static void main(String[] args) {

		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String serviceName = args[2];
		// locate the registry and get ror.
		RegistryCommunicator rc = null;
		try {
			rc = MyLocateRegistry.getRegistry(host, port);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		RemoteObjectReference ror = null;
		try {
			ror = rc.lookup(serviceName);
		} catch (MyRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// get the proxy
		NameServer nameServerFirst = (NameServer) ror.localize();
		System.out.println(nameServerFirst.getServiceName());
		NameServer nameServerSecond = nameServerFirst.add(serviceName + "2",
				((RemoteObjectInvocationHandler) Proxy
						.getInvocationHandler(nameServerFirst)).getStub()
						.getRemoteObjectReference(), nameServerFirst);
		System.out.println(nameServerSecond.getServiceName());
		NameServer nameServerThird = nameServerSecond.add(serviceName + "3",
				((RemoteObjectInvocationHandler) Proxy
						.getInvocationHandler(nameServerSecond)).getStub()
						.getRemoteObjectReference(), nameServerSecond);
		System.out.println(nameServerThird.getServiceName());
		NameServer nameServerNext = nameServerThird.next();
		System.out.println(nameServerNext.getServiceName());
		NameServer nameServerMatched = (NameServer) (nameServerThird
				.match(serviceName + "3").localize());
		System.out.println(nameServerMatched.getServiceName());

	}
}
