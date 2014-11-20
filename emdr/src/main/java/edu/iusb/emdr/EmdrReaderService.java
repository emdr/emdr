package edu.iusb.emdr;

import java.util.concurrent.ExecutorService;
import java.util.zip.Inflater;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

/**
 * The relay reader service. Connects to the relay's message queue and hands off the market object
 * to an <code>ExecutorService</code> persister to write it to the database.
 */
public class EmdrReaderService {

	private final ExecutorService executor;

	/**
	 * Executor provided constructor
	 * @param executor
	 */
	public EmdrReaderService(ExecutorService executor) {
		this.executor = executor;
	}

	/**
	 * Reads from the relay message queue.
	 * @throws Exception
	 */
	public void read() throws Exception {
		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket subscriber = context.socket(ZMQ.SUB);

		// Connect to the first publicly available relay.
		subscriber.connect("tcp://relay-us-central-1.eve-emdr.com:8050");

		// Disable filtering.
		subscriber.subscribe(new byte[0]);

		while (true) {
			try {
				JSONObject md = getMarketData(subscriber);
				execute(md);
			} catch (ZMQException ex) {
				System.out.println("ZMQ Exception occurred : "
						+ ex.getMessage());
			}
		}
	}

	/**
	 * Receives message from subscriber and returns parsed json object. 
	 * @param subscriber
	 * @return parsed json object
	 * @throws Exception
	 */
	protected JSONObject getMarketData(ZMQ.Socket subscriber) throws Exception {
		// Receive compressed raw market data.
		byte[] receivedData = subscriber.recv(0);

		// We build a large enough buffer to contain the decompressed data.
		byte[] decompressed = new byte[receivedData.length * 16];

		// Decompress the raw market data.
		Inflater inflater = new Inflater();
		inflater.setInput(receivedData);
		int decompressedLength = inflater.inflate(decompressed);
		inflater.end();

		byte[] output = new byte[decompressedLength];
		System.arraycopy(decompressed, 0, output, 0, decompressedLength);

		// Transform data into JSON strings.
		String market_json = new String(output, "UTF-8");

		// Un-serialize the JSON data.
		JSONParser parser = new JSONParser();
		JSONObject marketData = (JSONObject) parser.parse(market_json);

		return marketData;

	}

	protected void execute(JSONObject marketData) {
		// you know, do more fun things here.
		RelayItemPersisterTask task = new RelayItemPersisterTask(marketData);
		executor.execute(task);
	}

}
