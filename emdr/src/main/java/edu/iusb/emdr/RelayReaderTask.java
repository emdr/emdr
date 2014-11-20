package edu.iusb.emdr;

import java.util.concurrent.ExecutorService;

/**
 * Wraps a <code>Reader</code> around a <code>Runnable</code> to be
 * executed by a <code>Thread</code> and therefore an <code>Executor</code> as well.
 * Uses an executor to manage children tasks.
 */
public class RelayReaderTask implements Runnable {
	private ExecutorService executor;

	/**
	 * Constructor
	 * @param executor children task manager
	 */
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
