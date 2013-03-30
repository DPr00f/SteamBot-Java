package uk.co.thomasc.scrapbanktf.trade;

import org.json.simple.JSONObject;

public class TradeItem {

	//public long id;

	public int classId;

	public int instanceId;

	public TradeItem(JSONObject value) {
		//id = (long) value.get("id");
		classId = Integer.parseInt((String) value.get("classid"));
		instanceId = Integer.parseInt((String) value.get("instanceid"));
	}

}
