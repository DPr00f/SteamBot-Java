package uk.co.thomasc.scrapbanktf.trade;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONObject;

public class TradeInventory {

	public boolean success = false;

	private final Map<Long, TradeItem> items = new HashMap<Long, TradeItem>();
	private final Map<String, TradeItemDescription> descriptions = new HashMap<String, TradeItemDescription>();

	@SuppressWarnings("unchecked")
	public TradeInventory(JSONObject parse) {
		success = (boolean) parse.get("success");

		Object obj = parse.get("rgInventory");
		Iterator<Entry<String, JSONObject>> iterator;
		if (obj instanceof HashMap<?, ?>) {
			iterator = ((HashMap<String, JSONObject>) obj).entrySet().iterator();
			while (iterator.hasNext()) {
				final Entry<String, JSONObject> row = iterator.next();
				items.put(Long.parseLong(row.getKey()), new TradeItem(row.getValue()));
			}
		}

		obj = parse.get("rgDescriptions");
		if (obj instanceof HashMap<?, ?>) {
			iterator = ((HashMap<String, JSONObject>) obj).entrySet().iterator();
			while (iterator.hasNext()) {
				final Entry<String, JSONObject> row = iterator.next();
				descriptions.put(row.getKey(), new TradeItemDescription(row.getValue()));
			}
		}
	}

	public TradeInventory() {

	}

	public TradeItem get(long child) {
		return items.get(child);
	}

	public TradeItemDescription getDescription(String string) {
		return descriptions.get(string);
	}

	public Set<Long> getIds() {
		return items.keySet();
	}

}
