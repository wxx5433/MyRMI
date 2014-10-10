package TestService.ZipCodeRList;

import Remote.Remote640;

public interface ZipCodeRList extends Remote640 {
	public String find(String city);

	public ZipCodeRList add(String city, String zipcode);

	public ZipCodeRList next();
}
