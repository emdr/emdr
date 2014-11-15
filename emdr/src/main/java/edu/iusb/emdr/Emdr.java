package edu.iusb.emdr;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Emdr {

	public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Runnable readerTask = new RelayReaderTask(executor);
//        executor.execute(reader);
        Thread readerThread = new Thread(readerTask);
        readerThread.setName("main");
        readerThread.start();
        try {
			readerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        executor.shutdown();
        System.out.println("Finished all threads");

	}

}
