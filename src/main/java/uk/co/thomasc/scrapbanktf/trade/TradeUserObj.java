package uk.co.thomasc.scrapbanktf.trade;

import org.json.simple.JSONObject;

public class TradeUserObj {
	public boolean ready;

	public boolean confirmed;

	public int sec_since_touch;

	public TradeUserObj(JSONObject obj) {
		ready = (long) obj.get("ready") == 1;
		confirmed = (long) obj.get("confirmed") == 1;
		sec_since_touch = (int) (long) obj.get("sec_since_touch");
	}
}
