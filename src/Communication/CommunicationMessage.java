package Communication;

import java.io.Serializable;

public class CommunicationMessage implements Serializable {

	private static final long serialVersionUID = 3871209197799810883L;

	public enum MessageType {
		NewService(1), // New Service is started on one dispatch server
		ReplyToServer(2), // Reply to dispatch server
		LookUpService(3), // Look up service request from client
		ReplyROR(4); // Reply remoteObjectReference to the client

		private int messageType;

		private MessageType(int messageType) {
			this.messageType = messageType;
		}

	}

	private String message;
	private MessageType messageType;

	public CommunicationMessage(MessageType messageType, String message) {
		this.message = message;
		this.messageType = messageType;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

}
