package DispatchNode;

import java.io.ObjectOutputStream;
import java.io.OutputStream;

import Remote.Remote640;
import Utils.InvokeTask;

/**
 * This thread takes responsibility to take one invoke request from block queue
 * This design will not block the main process of </code>DispatchNode</code>
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 *
 */
public class DispatchExecutionThread implements Runnable {

	private InvokeTask invokeTask;

	public DispatchExecutionThread(InvokeTask invokeTask) {
		this.invokeTask = invokeTask;
	}

	@Override
	public void run() {
		Remote640 serviceObject = invokeTask.getServiceObject();
		try {
			invokeTask.getMessage().call(serviceObject);
			OutputStream outputStream = invokeTask.getSocket().getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(invokeTask.getMessage());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void terminate() {

	}

}
