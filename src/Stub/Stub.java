package Stub;

import Remote.RemoteObjectReference;

public abstract class Stub {
	RemoteObjectReference ror;
	
	public void setRemoteObjectReference(RemoteObjectReference ror) {
		this.ror = ror;
	}
	public RemoteObjectReference getRemoteObjectReference() {
		return ror;
	}
}
