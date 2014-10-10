package TestService.ZipCodeServer;

import java.io.Serializable;

public class ZipCodeList implements Serializable {
	public String city;
	public String ZipCode;
	public ZipCodeList next;

	public ZipCodeList(String c, String z, ZipCodeList n) {
		city = c;
		ZipCode = z;
		next = n;
	}
}
