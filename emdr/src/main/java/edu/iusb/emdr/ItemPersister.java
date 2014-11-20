package edu.iusb.emdr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.json.simple.JSONObject;

/**
 * Persists market data json objects to the database.
 */
public class ItemPersister {
	private Connection conn = null;
	private PreparedStatement ps = null;

	private final JSONObject marketData;

	/**
	 * Market data constructor.
	 * @param marketData market data object to persist
	 */
	public ItemPersister(JSONObject marketData) {
		this.marketData = marketData;
	}

	// TODO: switch to connection pool
	/**
	 * Saves the object to the database.
	 * @throws Exception
	 */
	public void persist() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://localhost/emdr?"
				+ "user=emdr&password=emdrpasswd");
		ps = conn.prepareStatement("INSERT INTO items_buying "
				+ " (region_id) " 
				+ " VALUES " 
				+ " ( ? )" 
				+ " ;");
		// TODO: pull out the items correctly, this isn't right...
		//ps.setInt(0, Integer.parseInt(marketData.get("regionID").toString()));
		ps.setInt(0, 10000060);
		ps.executeUpdate();
		conn.close();
		System.out.println("persisting... " + marketData);
		return;
	}
}
