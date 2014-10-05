package ProxyTest;

public class HelloWorldImpl implements HelloWorld {

	@Override
	public void sayHelloWorld() {
		System.out.println("HelloWorld!");
	}

	@Override
	public void sayHelloWorld2(int i) {
		System.out.println("HelloWorld------------!" + i);

	}

}
