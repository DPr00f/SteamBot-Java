package uk.co.thomasc.scrapbanktf.trade;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

public class StatusObj {
	public String error;

	public boolean newversion;

	public boolean success;

	public long trade_status = -1;

	public int version;

	public int logpos;

	public TradeUserObj me;

	public TradeUserObj them;

	public List<TradeEvent> events = new ArrayList<TradeEvent>();

	@SuppressWarnings("unchecked")
	public StatusObj(JSONObject obj) {
		success = (boolean) obj.get("success");
		if (success) {
			error = "None";
			trade_status = (long) obj.get("trade_status");

			if (trade_status == 0) {
				newversion = (boolean) obj.get("newversion");
				version = (int) (long) obj.get("version");
				if (obj.containsKey("logpos")) {
					logpos = (int) obj.get("logpos");
				}
				me = new TradeUserObj((JSONObject) obj.get("me"));
				them = new TradeUserObj((JSONObject) obj.get("them"));
				for (final JSONObject event : (ArrayList<JSONObject>) obj.get("events")) {
					events.add(new TradeEvent(event));
				}
			}
		} else {
			error = (String) obj.get("error");
		}
	}
}
