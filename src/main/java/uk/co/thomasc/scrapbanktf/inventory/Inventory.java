package uk.co.thomasc.scrapbanktf.inventory;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import uk.co.thomasc.scrapbanktf.BotInfo;
import uk.co.thomasc.scrapbanktf.util.Util;

public class Inventory {

	public static Inventory fetchInventory(long steamId) {
		return fetchInventory(steamId, true);
	}
	
	public static Inventory fetchInventory(long steamId, boolean useCache) {
		final String response = Util.webRequest("http://api.steampowered.com/IEconItems_440/GetPlayerItems/v0001/?key=" + BotInfo.getApiKey() + "&SteamID=" + steamId + "&format=json", useCache);
		try {
			final JSONObject ret = (JSONObject) new JSONParser().parse(response);
			return new Inventory((JSONObject) ret.get("result"));
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public long numSlots;
	public List<Item> items = new ArrayList<Item>();

	@SuppressWarnings("unchecked")
	private Inventory(JSONObject jsonObject) {
		numSlots = (long) jsonObject.get("num_backpack_slots");
		for (final JSONObject obj : (ArrayList<JSONObject>) jsonObject.get("items")) {
			items.add(new Item(obj));
		}
	}

	public Item getItem(Long id) {
		for (final Item item : items) {
			if (item.id == id) {
				return item;
			}
		}
		return null;
	}

	public List<Long> getItemIds() {
		final List<Long> result = new ArrayList<Long>();
		for (final Item item : items) {
			result.add(item.id);
		}
		return result;
	}

	public List<Integer> getItems() {
		final List<Integer> result = new ArrayList<Integer>();
		for (final Item item : items) {
			result.add(item.defIndex);
		}
		return result;
	}

}
