package uk.co.thomasc.scrapbanktf.inventory;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

public class Item {

	public long id;

	public long originalId;

	public int defIndex;

	public byte level;

	public byte quality;

	//public int position;

	public boolean isNotCraftable;

	public List<ItemAttribute> attributes = new ArrayList<ItemAttribute>();

	@SuppressWarnings("unchecked")
	Item(JSONObject obj) {
		id = (long) obj.get("id");
		originalId = (long) obj.get("original_id");
		defIndex = (int) (long) obj.get("defindex");
		level = (byte) (long) obj.get("level");
		quality = (byte) (long) obj.get("quality");
		//position = (int) obj.get("pos");
		final Object flag = obj.get("flag_cannot_craft");
		isNotCraftable = flag == null ? false : (boolean) flag;
		final Object attrs = obj.get("attributes");
		if (attrs != null && attrs instanceof ArrayList<?>) {
			for (final JSONObject attr : (ArrayList<JSONObject>) attrs) {
				attributes.add(new ItemAttribute(attr));
			}
		}
	}

}
