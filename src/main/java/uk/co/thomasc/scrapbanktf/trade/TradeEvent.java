package uk.co.thomasc.scrapbanktf.trade;

import org.json.simple.JSONObject;

public class TradeEvent {
	public String steamid;

	public int action;

	public long timestamp;

	public int appid;

	public String text;

	//public int contextid;

	public long assetid;

	public TradeEvent(JSONObject event) {
		steamid = (String) event.get("steamid");
		action = Integer.parseInt((String) event.get("action"));
		timestamp = (long) event.get("timestamp");
		appid = (int) (long) event.get("appid");
		text = (String) event.get("text");
		//contextid = (int) event.get("contextid");
		if (event.containsKey("assetid")) {
			assetid = Long.valueOf((String) event.get("assetid"));
		}
	}
}
