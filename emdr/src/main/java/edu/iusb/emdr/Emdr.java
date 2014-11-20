package edu.iusb.emdr;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Emdr starts the main reader thread and waits for it to terminate.
 */
public class Emdr {

	/**
	 * Eve market data relay reader entry point.
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Runnable readerTask = new RelayReaderTask(executor);
//        executor.execute(reader);
        Thread readerThread = new Thread(readerTask);
        readerThread.setName("main");
        readerThread.start();
        try {
        	// sleep until the reader terminates
			readerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        executor.shutdown();
        System.out.println("Finished all threads");

	}

}
