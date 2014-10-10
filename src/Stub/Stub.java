package Stub;

import java.io.Serializable;

import Remote.RemoteObjectReference;

public abstract class Stub implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6772989880784622650L;
	RemoteObjectReference ror;

	public void setRemoteObjectReference(RemoteObjectReference ror) {
		this.ror = ror;
	}

	public RemoteObjectReference getRemoteObjectReference() {
		return ror;
	}
}
