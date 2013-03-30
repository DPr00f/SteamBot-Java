package uk.co.thomasc.scrapbanktf.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import uk.co.thomasc.scrapbanktf.Bot;
import uk.co.thomasc.scrapbanktf.Main;

public class Util {

	private static JSONObject itemSchema;
	private static Map<Integer, ItemInfo> itemInfo = new HashMap<Integer, ItemInfo>();
	private static Map<String, Response> webResponses = new HashMap<String, Response>();

	static {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(Util.class.getResourceAsStream("/schema440-en_US.json")));
		try {
			Util.itemSchema = (JSONObject) new JSONParser().parse(reader);
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ParseException e) {
			e.printStackTrace();
		}
	}

	public static String removeArg0(String a) {
		return a.contains(" ") ? a.substring(a.indexOf(" ") + 1) : "";
	}

	public static ItemInfo getItemInfo(int itemid) {
		if (!Util.itemInfo.containsKey(itemid)) {
			Util.itemInfo.put(itemid, new ItemInfo(itemid, (JSONObject) Util.itemSchema.get(String.valueOf(itemid))));
		}
		return Util.itemInfo.get(itemid);
	}

	public static boolean isDebugMode = true;
	private static Object lck = new Object();

	public static void printConsole(String line) {
		Util.printConsole(line, 0, ConsoleColor.White, true);
	}

	public static void printConsole(String line, Bot bot) {
		Util.printConsole(line, bot.getBotId());
	}
	
	public static void printConsole(String line, int bot) {
		Util.printConsole(line, bot, ConsoleColor.White);
	}
	
	public static void printConsole(String line, Bot bot, ConsoleColor color) {
		Util.printConsole(line, bot.getBotId(), color);
	}
	
	public static void printConsole(String line, int bot, ConsoleColor color) {
		Util.printConsole(line, bot, color, false);
	}

	public static void printConsole(String line, Bot bot, ConsoleColor color, boolean isDebug) {
		Util.printConsole(line, bot.getBotId(), color, isDebug);
	}
	
	public static void printConsole(String line, int bot, ConsoleColor color, boolean isDebug) {
		synchronized (Util.lck) {
			final String lineC = color.v() + line + ConsoleColor.Reset.v();
			if (isDebug && Util.isDebugMode) {
				System.out.println("(" + bot + ") [DEBUG] " + lineC.replace("\n", "\n(" + bot + ") [DEBUG] "));
			} else if (!isDebug) {
				System.out.println("(" + bot + ")         " + lineC.replace("\n", "\n(" + bot + ")         "));
				//Main.sql.query("INSERT INTO botLogs (botid, message, color) VALUES ('" + bot + "', '" + line.substring(0, Math.min(line.length(), 255)) + "', '" + color.getInt() + "')");
			}
		}
	}

	public static String webRequest(String url) {
		return webRequest(url, true);
	}
	
	public static String webRequest(String url, boolean useCache) {
		if (useCache && Util.webResponses.containsKey(url)) {
			if (Util.webResponses.get(url).isRecent()) {
				return Util.webResponses.get(url).getResponse();
			}
		}
		String out = "";
		try {
			final URL url2 = new URL(url);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(url2.openStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				if (out.length() > 0) {
					out += "\n";
				}
				out += line;
			}
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		Util.webResponses.put(url, new Response(out));
		return out;
	}

	public static LinkedHashMap<Integer, MutableInt> sortHashMapByValues(HashMap<Integer, MutableInt> passedMap, boolean ascending) {
		final List<Integer> mapKeys = new ArrayList<Integer>(passedMap.keySet());
		final List<MutableInt> mapValues = new ArrayList<MutableInt>(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		if (!ascending) {
			Collections.reverse(mapValues);
		}

		final LinkedHashMap<Integer, MutableInt> someMap = new LinkedHashMap<Integer, MutableInt>();
		final Iterator<MutableInt> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			final MutableInt val = valueIt.next();
			final Iterator<Integer> keyIt = mapKeys.iterator();
			while (keyIt.hasNext()) {
				final Integer key = keyIt.next();
				if (passedMap.get(key).toString().equals(val.toString())) {
					passedMap.remove(key);
					mapKeys.remove(key);
					someMap.put(key, val);
					break;
				}
			}
		}
		return someMap;
	}
}
