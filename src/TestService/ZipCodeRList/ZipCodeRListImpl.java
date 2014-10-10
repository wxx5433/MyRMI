package TestService.ZipCodeRList;

import java.io.IOException;

import MyRMIRegistry.MyLocateRegistry;
import MyRMIRegistry.RegistryCommunicator;

public class ZipCodeRListImpl implements ZipCodeRList {
	String city;
	String zipcode;
	ZipCodeRList next;

	// this constructor creates the terminal of the list.
	// it is assumed this is called at the outset.
	public ZipCodeRListImpl() {
		city = null;
		zipcode = null;
		next = null;
	}

	// this is the standard constructor.
	public ZipCodeRListImpl(String c, String z, ZipCodeRList n) {
		city = c;
		zipcode = z;
		next = n;
	}

	// finding the zip code only for that cell.
	// its client can implement recursive search.
	@Override
	public String find(String c) {
		if (c.equals(city))
			return zipcode;
		else
			return null;
	}

	// this is essentially cons.
	@Override
	public ZipCodeRList add(String c, String z) {
		return new ZipCodeRListImpl(c, z, this);
	}

	// this is essentially car.
	@Override
	public ZipCodeRList next() {
		return next;
	}

	public static void main(String[] args) {
		RegistryCommunicator rc = null;
		try {
			rc = MyLocateRegistry.getRegistry();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rc.rebind(args[0], args[1], 11112);
	}
}