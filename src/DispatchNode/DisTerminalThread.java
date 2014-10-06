package DispatchNode;

import java.util.Scanner;

/**
 * This is the class that the <code>DispatchNode</code> use to run a terminal
 * thread, so that it can receive users' input.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 */
public class DisTerminalThread implements Runnable {
	/**
	 * The dispatchNode which this terminal thread belongs to.
	 */
	private DispatchNode dispatchNode;
	private boolean stop;

	public DisTerminalThread(DispatchNode dispatchNode) {
		this.dispatchNode = dispatchNode;
		stop = false;
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		System.out.print("****************************************\n"
				+ "Please enter your command:\n" + "1.Start a new service:\n"
				+ "exp:start ZipCodeService\n"
				+ "2.List all running services\n" + "exp:list\n"
				+ "3.Terminate a service with service name and key\n"
				+ "exp:terminate ZipCodeService:123456789\n" + "4.Exit\n"
				+ "exp:exit\n");
		while (!stop) {
			/* Receive User's command */
			Scanner keyboard = new Scanner(System.in);
			String command = keyboard.nextLine();
			/* parse the command */
			dispatchNode.parseCommand(command);
		}
	}

	/**
	 * Terminate the terminal thread.
	 */
	public void terminate() {
		stop = true;
	}
}
