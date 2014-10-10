package TestService.ZipCodeServer;

import Remote.Remote640;

public interface ZipCodeServer extends Remote640 {
	public void initialize(ZipCodeList newlist);

	public String find(String city);

	public ZipCodeList findAll();

	public void printAll();
}
