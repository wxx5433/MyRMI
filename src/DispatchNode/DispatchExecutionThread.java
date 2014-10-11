package DispatchNode;

import java.io.ObjectOutputStream;
import java.io.OutputStream;

import Remote.Remote640;
import Remote.RemoteObjectReference;
import Util.InvokeTask;

/**
 * This thread takes responsibility to take one invoke request from block queue
 * This design will not block the main process of </code>DispatchNode</code>
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 *
 */
public class DispatchExecutionThread implements Runnable {

	private DispatchNode dispatchNode;
	private InvokeTask invokeTask;

	public DispatchExecutionThread(InvokeTask invokeTask,
			DispatchNode dispatchNode) {
		this.invokeTask = invokeTask;
		this.dispatchNode = dispatchNode;
	}

	@Override
	public void run() {
		Remote640 serviceObject = invokeTask.getServiceObject();
		System.out.println("start to do method----"
				+ invokeTask.getMessage().getMethodName());
		try {
			invokeTask.getMessage().call(serviceObject);
			if (invokeTask.getMessage().getReturnValue() instanceof Remote640) {
				System.out.println("The return value is Proxy! Create a ROR");
				RemoteObjectReference ror = dispatchNode.getROR(invokeTask);
				invokeTask.getMessage().setReturnValue(ror);
				invokeTask.getMessage().setReturnROR(true);
			}
			System.out.println("Send back result----"
					+ invokeTask.getMessage().getMethodName());
			if (invokeTask.getMessage().getReturnValue() instanceof RemoteObjectReference) {
				System.out.println(((RemoteObjectReference) (invokeTask
						.getMessage().getReturnValue())).getHostIP());
			}
			OutputStream outputStream = invokeTask.getSocket()
					.getOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					outputStream);
			objectOutputStream.writeObject(invokeTask.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void terminate() {

	}

}
