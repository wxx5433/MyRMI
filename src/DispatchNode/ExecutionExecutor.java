package DispatchNode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Util.InvokeTask;

/**
 * {@link ExecutionExecutor} is thread pool management thread to control how
 * many {@link ExecutionThread} threads can run at the same time. Exeuctor will
 * try to get {@link SplitTask} from {@link SplitTasksMangement}, if there is
 * split task, it will use thread pool to lauch a thread.
 * 
 * 
 */

class ExecutionExecutor implements Runnable {

	DispatchNode dispatchNode;
	volatile boolean isStop;
	private ExecutorService pool;

	public ExecutionExecutor(DispatchNode dispatchNode) {
		this.dispatchNode = dispatchNode;
		this.isStop = false;
		pool = Executors.newFixedThreadPool(dispatchNode.getSlotNumber());
	}

	@Override
	public void run() {
		while (!isStop) {
			try {
				InvokeTask invokeTask = dispatchNode.invokeRequestQueue.take();
				execute(invokeTask);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (isStop)
				break;
		}
	}

	public void execute(InvokeTask invokeTask) {
		DispatchExecutionThread dispatchExecutionThread = new DispatchExecutionThread(
				invokeTask, dispatchNode);
		Thread thread = new Thread(dispatchExecutionThread);
		pool.execute(thread);
	}

	public void terminate() {
		pool.shutdown();
		isStop = true;
	}

}
