package TestService.NameService;

import Remote.Remote640;
import Remote.RemoteObjectReference;

public interface NameServer extends Remote640 {
	public RemoteObjectReference match(String name);

	public NameServer add(String s, RemoteObjectReference r, NameServer n);

	public NameServer next();

	public String getServiceName();
}
