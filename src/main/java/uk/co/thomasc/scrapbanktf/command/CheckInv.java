package uk.co.thomasc.scrapbanktf.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import uk.co.thomasc.scrapbanktf.BotInfo;
import uk.co.thomasc.scrapbanktf.util.ItemInfo;
import uk.co.thomasc.scrapbanktf.util.MutableInt;
import uk.co.thomasc.scrapbanktf.util.Util;

public class CheckInv extends Command {

	@SuppressWarnings("unchecked")
	@Override
	public String run(CommandInfo cmdInfo) {
		String out = "Here are the items you have duplicates of:";
		final String response = Util.webRequest("http://api.steampowered.com/ITFItems_440/GetPlayerItems/v0001/?key=" + BotInfo.getApiKey() + "&SteamID=" + cmdInfo.getSteamid() + "&format=json");

		final int limit = cmdInfo.getArg(0, 2);
		try {
			final JSONObject ret = (JSONObject) new JSONParser().parse(response);

			final JSONArray obj = (JSONArray) ((JSONObject) ((JSONObject) ret.get("result")).get("items")).get("item");
			final HashMap<Integer, MutableInt> freq = new HashMap<Integer, MutableInt>();
			for (final JSONObject i : (ArrayList<JSONObject>) obj) {
				final int defindex = (int) (long) i.get("defindex");
				if (freq.containsKey(defindex)) {
					freq.get(defindex).increment();
				} else {
					freq.put(defindex, new MutableInt());
				}
			}

			final Map<Integer, MutableInt> sortedFreq = Util.sortHashMapByValues(freq, true);
			for (final Entry<Integer, MutableInt> entry : sortedFreq.entrySet()) {
				if (entry.getValue().get() < limit) {
					break;
				}
				final ItemInfo itemInfo = Util.getItemInfo(entry.getKey());
				out += "\n" + itemInfo.getName() + " x" + entry.getValue().get();
			}
			return out;
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		return "I fell over, sorry :<";
	}

}
