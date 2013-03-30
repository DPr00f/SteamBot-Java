package uk.co.thomasc.scrapbanktf.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ItemInfo {

	private final String name;
	private final String itemClass;
	private final Map<Integer, Double> attributes = new HashMap<Integer, Double>();

	@SuppressWarnings("unchecked")
	public ItemInfo(int itemid, JSONObject info) {
		itemClass = (String) info.get("item_class");
		name = (String) info.get("item_name");
		if (info.containsKey("attributes")) {
			if (info.get("attributes") instanceof JSONArray) {
				final JSONArray arr = (JSONArray) info.get("attributes");
				for (final JSONObject obj : (List<JSONObject>) arr) {
					addAttribute(obj);
				}
			} else {
				for (final Entry<String, JSONObject> attr : (Set<Entry<String, JSONObject>>) ((JSONObject) info.get("attributes")).entrySet()) {
					addAttribute(attr.getValue());
				}
			}
		}
	}

	private void addAttribute(JSONObject obj) {
		final Object val = obj.get("value");
		if (val instanceof Long) {
			attributes.put((int) (long) obj.get("defindex"), (double) (long) val);
		} else if (val instanceof Double) {
			attributes.put((int) (long) obj.get("defindex"), (double) val);
		} else {
			Util.printConsole("wut?" + val.getClass());
		}
	}

	public String getName() {
		if (itemClass.equalsIgnoreCase("supply_crate")) {
			return name + " #" + attributes.get(187).intValue();
		}
		return name;
	}

}
