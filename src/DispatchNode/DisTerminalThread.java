package DispatchNode;

import java.util.Scanner;

import RegistrationNode.RegistrationNode;

/**
 * This is the class that the <code>MasterNode</code> use to run a terminal
 * thread, so that it can receive users' input.
 * 
 * @author Xiaoxiang Wu(xiaoxiaw)
 * @author Ye Zhou(yezhou)
 */
public class DisTerminalThread implements Runnable {
	/**
	 * The masterNode which this terminal thread belongs to.
	 */
	private RegistrationNode masterNode;
	private boolean stop;

	public DisTerminalThread(RegistrationNode masterNode) {
		this.masterNode = masterNode;
		stop = false;
	}

	@Override
	public void run() {
		System.out.print("****************************************\n"
				+ "Please enter your command:\n" + "1.Start a new service:\n"
				+ "exp:start ZipCodeService\n"
				+ "5.List all running services\n" + "exp:list\n"
				+ "6.Terminate a service\n"
				+ "exp:terminate 128.237.213.96:8888\n"
				+ "7.Terminate a process in a slave\n"
				+ "exp:processterminate 128.237.213.96:8888 10110\n"
				+ "8.Exit\n" + "exp:exit\n");
		while (!stop) {
			/* Receive User's command */
			Scanner keyboard = new Scanner(System.in);
			String command = keyboard.nextLine();
			/* parse the command */
			masterNode.parseCommand(command);
		}
	}

	/**
	 * Terminate the terminal thread.
	 */
	public void terminate() {
		stop = true;
	}
}
