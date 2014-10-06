package Utils;

import java.io.ObjectOutputStream;

import Communication.RMIMessage;
import Remote.Remote640;

public class InvokeTask {
	private RMIMessage message;
	private ObjectOutputStream outputStream;
	private Remote640 serviceObject;

	public InvokeTask(RMIMessage message, ObjectOutputStream outputStream,
			Remote640 serviceObject) {
		this.message = message;
		this.outputStream = outputStream;
		this.serviceObject = serviceObject;
	}

	public RMIMessage getMessage() {
		return message;
	}

	public void setMessage(RMIMessage message) {
		this.message = message;
	}

	public ObjectOutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(ObjectOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public Remote640 getServiceObject() {
		return serviceObject;
	}

	public void setServiceObject(Remote640 serviceObject) {
		this.serviceObject = serviceObject;
	}

}
