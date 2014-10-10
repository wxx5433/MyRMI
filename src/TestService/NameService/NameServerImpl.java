package TestService.NameService;

import java.io.IOException;

import MyRMIRegistry.MyLocateRegistry;
import MyRMIRegistry.RegistryCommunicator;
import Remote.RemoteObjectReference;

public class NameServerImpl implements NameServer {

	String serviceName;
	RemoteObjectReference ro;
	NameServer next;

	public NameServerImpl() {
		serviceName = "";
		ro = null;
		next = null;
	}

	public NameServerImpl(String s, RemoteObjectReference r, NameServer n) {
		serviceName = s;
		ro = r;
		next = n;
	}

	@Override
	public NameServer add(String s, RemoteObjectReference r, NameServer n) {
		return new NameServerImpl(s, r, this);
	}

	@Override
	public RemoteObjectReference match(String name) {
		if (name.equals(serviceName))
			return ro;
		else
			return null;
	}

	@Override
	public NameServer next() {
		return next;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	public static void main(String[] args) {
		RegistryCommunicator rc = null;
		try {
			rc = MyLocateRegistry.getRegistry();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rc.rebind(args[0], args[1], 11112);
	}

}