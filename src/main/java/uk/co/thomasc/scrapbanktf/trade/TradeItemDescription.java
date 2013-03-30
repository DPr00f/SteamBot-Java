package uk.co.thomasc.scrapbanktf.trade;

import org.json.simple.JSONObject;

public class TradeItemDescription {

	public String name;

	public TradeItemDescription(JSONObject value) {
		name = (String) value.get("name");
	}

}
