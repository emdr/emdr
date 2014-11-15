package edu.iusb.emdr;

import java.util.concurrent.ExecutorService;

public class RelayReaderTask implements Runnable {
	private ExecutorService executor;

	public RelayReaderTask(ExecutorService executor) {
		this.executor = executor;
	}

	public void run() {
		EmdrReaderService reader = new EmdrReaderService(executor);
		try {
			reader.read();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
