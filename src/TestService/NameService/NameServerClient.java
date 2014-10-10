package TestService.NameService;

import java.io.IOException;

import Exception.MyRemoteException;
import MyRMIRegistry.MyLocateRegistry;
import MyRMIRegistry.RegistryCommunicator;
import Remote.RemoteObjectReference;

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
		NameServer nameServerSecond = nameServerFirst.add(serviceName + "2",
				ror, nameServerFirst);
		NameServer nameServerThird = nameServerSecond.add(serviceName + "3",
				ror, nameServerSecond);
		NameServer nameServerMatched = (NameServer) nameServerThird.next()
				.match(serviceName).localize();
		System.out.println(nameServerMatched.getServiceName());

	}
}
