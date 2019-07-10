

import java.util.Date;

public class Wrapper {

	public static void main(String[] args) {
		System.out.println("Wrapper Started!  ==> "+ new Date());
		ExtractThread extThread = new ExtractThread();
		try {
			extThread.start();

			extThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Wrapper Completed! => "+ new Date());
	}

}
