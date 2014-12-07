package edu.iusb.emdr;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Persists market data json objects to the database.
 */
public class ItemPersister {
	private static final int ROW_PRICE_COLID = 0;
	private static final int ROW_VOLREMAINING_COLID = ROW_PRICE_COLID + 1;
	private static final int ROW_RANGE_COLID = ROW_VOLREMAINING_COLID + 1;
	private static final int ROW_ORDERID_COLID = ROW_RANGE_COLID + 1;
	private static final int ROW_VOLENTERED_COLID = ROW_ORDERID_COLID + 1;
	private static final int ROW_MINVOLUME_COLID = ROW_VOLENTERED_COLID + 1;
	private static final int ROW_BID_COLID = ROW_MINVOLUME_COLID + 1;
	private static final int ROW_ISSUEDATE_COLID = ROW_BID_COLID + 1;
	private static final int ROW_DURATION_COLID = ROW_ISSUEDATE_COLID + 1;
	private static final int ROW_STATIONID_COLID = ROW_DURATION_COLID + 1;
	private static final int ROW_SOLARSYSTEMID_COLID = ROW_STATIONID_COLID + 1;
	
	private Connection conn = null;
	private PreparedStatement ibstmt = null;
	private PreparedStatement isstmt = null;

	private final JSONObject marketData;
	
	private final JSONObjectContainer root;

	/**
	 * Market data constructor.
	 * @param marketData market data object to persist
	 */
	public ItemPersister(JSONObject marketData) {
		root = new JSONObjectContainer(null, marketData);
		this.marketData = root.getValue();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/emdr?"
					+ "user=emdr&password=emdrpasswd");
			ibstmt = conn.prepareStatement("REPLACE INTO items_buying ("
					+ "order_id, type_id, marketgroup_id, station_id, solarsystem_id, "
					+ "region_id, price, range2, duration, qty_total, qty_avail, qty_min, "
					+ "date_issued, date_expires, date_created) VALUES ("
					+ " ?,?,?,?,?,"
					+ " ?,?,?,?,?,?,?,"
					+ " ?,?,?);");
			isstmt = conn.prepareStatement("REPLACE INTO items_selling ("
					+ "order_id, type_id, marketgroup_id, station_id, solarsystem_id, " // 1-5
					+ "region_id, price, range2, duration, qty_total, qty_avail, qty_min, "			// 6-12
					+ "date_issued, date_expires, date_created) VALUES ("							// 13-15
					+ " ?,?,?,?,?,"
					+ " ?,?,?,?,?,?,?,"
					+ " ?,?,?);");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int parseHeader() {
		String resultTypeVal = (String) marketData.get("resultType");
		
		if (resultTypeVal.equals("orders")) {
			parseOrders();
		}
		return 0;
	}
	/* order_id, type_id, marketgroup_id, group_code, station_id, solarsystem_id, region_id, price, range2, duration, qty_total, qty_avail, qty_min, date_issued, date_expires, date_created */
	/* "price","volRemaining","range","orderID","volEntered","minVolume","bid","issueDate","duration","stationID","solarSystemID"*/
	public int parseOrders() {
		List<?> columns = null;
		JSONArray rowsets = null;
		if (marketData.get("columns") instanceof List<?>)
			columns = (List<?>) marketData.get("columns");
		else
			return 0;
		if (marketData.get("rowsets") instanceof JSONArray)
			rowsets = (JSONArray) marketData.get("rowsets");
		else
			return 0;
		JSONArrayContainer joCont = new JSONArrayContainer(root, rowsets);
		Iterator<?> it = rowsets.iterator();
		while (it.hasNext()) {
			JSONObjectContainer cont = new JSONObjectContainer(joCont, (JSONObject) it.next());
			Long regionID = (Long) cont.getValue().get("regionID");
			if (regionID == 10000043 || regionID == 10000032 || regionID == 10000002 || regionID == 10000030) {
				persistRowset(columns, cont);
			}
		}
		return 0;
	}
	
	private void persistRowset(List<?> cols, JSONObjectContainer cont) {
		JSONArray rows = (JSONArray) cont.getValue().get("rows");
		Iterator<?> it = rows.iterator();
		while (it.hasNext()) {
			JSONArrayContainer childCont = new JSONArrayContainer(cont, (JSONArray) it.next());
			persistRow(childCont);
		}
		
	}

	private void persistRow(JSONArrayContainer rowCont) {
		try {
			JSONObjectContainer rowset = (JSONObjectContainer) rowCont.getParent();
			PreparedStatement stmt;
			if ((boolean) rowCont.getValue().get(ROW_BID_COLID))
				stmt = ibstmt;
			else
				stmt = isstmt;
			stmt.setLong(1, (long) rowCont.getValue().get(ROW_ORDERID_COLID));
			stmt.setInt(2, (int)((long) rowset.getValue().get("typeID")));	// type id, int
			stmt.setInt(3, 0);	// market group, int
//			stmt.setInt(4, 0);	// group code, char
			stmt.setInt(4, (int)((long) rowCont.getValue().get(ROW_STATIONID_COLID)));
			stmt.setInt(5, (int)((long) rowCont.getValue().get(ROW_SOLARSYSTEMID_COLID)));
			stmt.setInt(6, (int)((long) rowset.getValue().get("regionID")));	// region_id
			stmt.setDouble(7, (double) rowCont.getValue().get(ROW_PRICE_COLID));
			stmt.setInt(8, (int)((long) rowCont.getValue().get(ROW_RANGE_COLID)));
			int duration = (int)((long) rowCont.getValue().get(ROW_DURATION_COLID));
			stmt.setInt(9, duration);
			stmt.setDouble(10, dbvalConv(rowCont.getValue().get(ROW_VOLREMAINING_COLID)));
			stmt.setDouble(11, dbvalConv(rowCont.getValue().get(ROW_VOLENTERED_COLID)));
			stmt.setDouble(12, dbvalConv(rowCont.getValue().get(ROW_MINVOLUME_COLID)));
			Calendar issueCal = DatatypeConverter.parseDateTime((String) rowCont.getValue().get(ROW_ISSUEDATE_COLID));
			Date issueDate = new Date(issueCal.getTimeInMillis());
			stmt.setDate(13, issueDate);
			stmt.setDate(14, new Date(issueCal.getTimeInMillis() + duration * 24 * 60 * 60 * 1000));
			Calendar genCal = DatatypeConverter.parseDateTime((String) rowset.getValue().get("generatedAt"));
			stmt.setDate(15, new Date(genCal.getTimeInMillis()));
			stmt.executeUpdate();
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	private double dbvalConv(Object val) {
		if (val instanceof Long)
			return ((Long)val).doubleValue();
		else if (val instanceof Double)
			return (double) val;
		else
			return 0.0;
	}
	
	public List<JSONObject> parseRowset(List<?> items) {
		System.out.println(items);
		return new ArrayList<JSONObject>();
		
	}

	// TODO: switch to connection pool
	/**
	 * Saves the object to the database.
	 * @throws Exception
	 */
	public void persist() throws Exception {
		System.out.println(marketData.toJSONString());
		parseHeader();
		conn.close();
		return;
	}
	
	private class JSONArrayContainer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6488413773159448551L;
		Object parent;
		final JSONArray jarray;
		public JSONArrayContainer(Object parent, JSONArray arr) {
			jarray = arr;
			this.parent = parent;
		}
		public Object getParent() {
			return parent;
		}
		public JSONArray getValue() {
			return jarray;
		}
	}
	
	private class JSONObjectContainer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2475592375029877693L;
		Object parent;
		final JSONObject jobj;
		public JSONObjectContainer(Object parent, JSONObject obj) {
			jobj = obj;
			this.parent = parent;
		}
		public Object getParent() {
			return parent;
		}
		public JSONObject getValue() {
			return jobj;
		}
	}
}

