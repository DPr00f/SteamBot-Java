package uk.co.thomasc.scrapbanktf.inventory;

import org.json.simple.JSONObject;

public class ItemAttribute {

	public short defIndex;

	public String value;

	ItemAttribute(JSONObject obj) {
		defIndex = (short) (long) obj.get("defindex");
		value = String.valueOf(obj.get("value"));
	}

}
