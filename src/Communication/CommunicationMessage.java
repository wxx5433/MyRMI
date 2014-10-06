package Communication;

import java.io.Serializable;

public class CommunicationMessage implements Serializable {

	private static final long serialVersionUID = 3871209197799810883L;

	public enum MessageType {
		NewDispatchOnline(0), // New Dispatch Server is online
		NewService(1), // New Service is started on one dispatch server
		DelService(2), // One Service is down on one dispatch server
		ReplyToDis(3), // Reply to dispatch server
		LookUpService(4), // Look up service request from client
		HeartBeat(5), // Look up service request from client
		ReplyServiceID(6); // Reply ServiceID to dispatch server

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
