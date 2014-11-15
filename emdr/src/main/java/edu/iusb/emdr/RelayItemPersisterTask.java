package edu.iusb.emdr;

import org.json.simple.JSONObject;

public class RelayItemPersisterTask implements Runnable {

	private final JSONObject marketData;
	
	public RelayItemPersisterTask(JSONObject marketData) {
		this.marketData = marketData;
	}

	public void run() {
		ItemPersister p = new ItemPersister(marketData);
		try {
			p.persist();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
