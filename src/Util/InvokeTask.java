package Util;

import java.net.Socket;

import Communication.RMIMessage;
import Remote.Remote640;

public class InvokeTask {
	private RMIMessage message;
	private Socket socket;
	private Remote640 serviceObject;

	public InvokeTask(RMIMessage message, Socket socket, Remote640 serviceObject) {
		this.message = message;
		this.socket = socket;
		this.serviceObject = serviceObject;
	}

	public RMIMessage getMessage() {
		return message;
	}

	public void setMessage(RMIMessage message) {
		this.message = message;
	}

	public Remote640 getServiceObject() {
		return serviceObject;
	}

	public void setServiceObject(Remote640 serviceObject) {
		this.serviceObject = serviceObject;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

}
