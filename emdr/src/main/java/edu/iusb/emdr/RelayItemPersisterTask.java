package edu.iusb.emdr;

import org.json.simple.JSONObject;

/**
 * Wraps a <code>Persister</code> around a <code>Runnable</code> to be
 * executed by a <code>Thread</code> and therefore an <code>Executor</code> as well.
 */
public class RelayItemPersisterTask implements Runnable {

	private final JSONObject marketData;
	
	/**
	 * Market Data constructor
	 * @param marketData the market data to persist.
	 */
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
