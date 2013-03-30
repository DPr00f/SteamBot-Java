package uk.co.thomasc.scrapbanktf.trade;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import uk.co.thomasc.scrapbanktf.SteamWeb;
import uk.co.thomasc.scrapbanktf.inventory.Inventory;
import uk.co.thomasc.scrapbanktf.inventory.Item;
import uk.co.thomasc.scrapbanktf.util.ItemInfo;
import uk.co.thomasc.scrapbanktf.util.Util;
import uk.co.thomasc.steamkit.types.steamid.SteamID;

public class Trade {
	// Static properties
	public static String SteamCommunityDomain = "steamcommunity.com";
	public static String SteamTradeUrl = "http://steamcommunity.com/trade/%s/";

	public SteamID meSID;
	public SteamID otherSID;

	// Generic Trade info
	public boolean meReady = false;
	public boolean otherReady = false;

	boolean tradeStarted = false;
	public Date TradeStart;
	public Date LastAction;

	int lastEvent = 0;
	public String pollLock2 = "";

	public int MaximumTradeTime = 360;
	public int MaximumActionGap = 60;

	// Items
	public Set<Long> MyTrade = new HashSet<Long>();
	public Set<Long> OtherTrade = new HashSet<Long>();
	public Object[] trades;

	public Inventory OtherInventory;
	public Inventory MyInventory;
	public Inventory[] inventories;

	// Internal properties needed for Steam API.
	protected String baseTradeURL;
	protected String steamLogin;
	protected String sessionId;
	protected int version = 1;
	protected int logpos;
	protected int numEvents;

	public TradeInventory OtherItems;
	public TradeInventory MyItems;

	public TradeListener tradeListener;

	public Trade(SteamID me, SteamID other, String sessionId, String token, TradeListener listener) throws Exception {
		meSID = me;
		otherSID = other;

		trades = new Object[] { MyTrade, OtherTrade };

		this.sessionId = sessionId;
		steamLogin = token;

		listener.trade = this;
		tradeListener = listener;

		baseTradeURL = String.format(Trade.SteamTradeUrl, otherSID.convertToLong());

		// try to poll for the first time
		/*try {
			Poll();
		} catch (Exception e) {
			Console.WriteLine(e.Message);
			Console.WriteLine(e.StackTrace);
			if (OnError != null)
				OnError(0);
			throw e;
		}*/

		try {
			sendMessage("Fetching data");

			// fetch the other player's inventory
			OtherItems = getInventory(otherSID);
			if (OtherItems == null || !OtherItems.success) {
				throw new Exception("Could not fetch other player's inventory via Trading!");
			}

			// fetch our inventory
			MyItems = getInventory(meSID);
			if (MyItems == null || !MyItems.success) {
				throw new Exception("Could not fetch own inventory via Trading!");
			}

			// fetch other player's inventory from the Steam API.
			OtherInventory = Inventory.fetchInventory(otherSID.convertToLong(), false);
			if (OtherInventory == null) {
				throw new Exception("Could not fetch other player's inventory via Steam API!");
			}

			// fetch our inventory from the Steam API.
			MyInventory = Inventory.fetchInventory(meSID.convertToLong(), false);
			if (MyInventory == null) {
				throw new Exception("Could not fetch own inventory via Steam API!");
			}

			sendMessage("Ready");
			inventories = new Inventory[] { MyInventory, OtherInventory };

			tradeListener.onAfterInit();

		} catch (final Exception e) {
			tradeListener.onError(3);
			e.printStackTrace();
			throw e;
		}

	}
	
	public StatusObj status = null;

	@SuppressWarnings("unchecked")
	public void Poll() {
		synchronized (pollLock2) {
			if (!tradeStarted) {
				tradeStarted = true;
				TradeStart = new Date();
				LastAction = new Date();
			}

			try {
				status = getStatus();
			} catch (final ParseException e) {
				e.printStackTrace();
				tradeListener.onError(1);
				return;
			}
			boolean isBot = true;

			// Update version
			if (status.newversion) {
				version = status.version;
			}

			if (lastEvent < status.events.size()) {
				for (; lastEvent < status.events.size(); lastEvent++) {
					final TradeEvent evt = status.events.get(lastEvent);
					isBot = !evt.steamid.equals(String.valueOf(otherSID.convertToLong()));

					switch (evt.action) {
						case 0:
							((Set<Long>) trades[isBot ? 0 : 1]).add(evt.assetid);
							if (!isBot) {
								final Item item = inventories[isBot ? 0 : 1].getItem(evt.assetid);
								final ItemInfo schemaItem = Util.getItemInfo(item.defIndex);
								tradeListener.onUserAddItem(schemaItem, item);
							}
							break;
						case 1:
							((Set<Long>) trades[isBot ? 0 : 1]).remove(evt.assetid);
							if (!isBot) {
								final Item item2 = inventories[isBot ? 0 : 1].getItem(evt.assetid);
								final ItemInfo schemaItem = Util.getItemInfo(item2.defIndex);
								tradeListener.onUserRemoveItem(schemaItem, item2);
							}
							break;
						case 2:
							if (!isBot) {
								otherReady = true;
								tradeListener.onUserSetReadyState(true);
							} else {
								meReady = true;
							}
							break;
						case 3:
							if (!isBot) {
								otherReady = false;
								tradeListener.onUserSetReadyState(false);
							} else {
								meReady = false;
							}
							break;
						case 4:
							if (!isBot) {
								tradeListener.onUserAccept();
							}
							break;
						case 7:
							if (!isBot) {
								tradeListener.onMessage(evt.text);
							}
							break;
						default:
							Util.printConsole("Unknown Event ID: " + evt.action);
							break;
					}

					if (!isBot) {
						LastAction = new Date();
					}
				}

			} else {
				// check if the user is AFK
				final Date now = new Date();

				final long untilActionTimeout = LastAction.getTime() / 1000 + MaximumActionGap - now.getTime() / 1000;
				final long untilTradeTimeout = TradeStart.getTime() / 1000 + MaximumTradeTime - now.getTime() / 1000;

				if (untilActionTimeout <= 0 || untilTradeTimeout <= 0) {
					tradeListener.onTimeout();
				} else if (untilActionTimeout <= 20 && untilActionTimeout % 5 == 0) {
					sendMessage("Are You AFK? The trade will be canceled in " + untilActionTimeout + " seconds if you don't do something.");
				}
			}

			if (status.trade_status == 3) {
				//Other user cancelled
				tradeListener.onError(2);
			} else if (status.trade_status == 4) {
				//Other user timed out, unlikely as we have a built-in timeout
				tradeListener.onError(4);
			} else if (status.trade_status == 5) {
				//Trade failed
				tradeListener.onError(5);
			} else if (status.trade_status == 1) {
				//Success
				tradeListener.onComplete();
			}

			// Update Local Variables
			if (status.them != null) {
				otherReady = status.them.ready;
				meReady = status.me.ready;
			}

			// Update version
			if (status.newversion) {
				tradeListener.onNewVersion();
			}

			if (status.logpos != 0) {
				Util.printConsole("WAT");
				logpos = status.logpos;
			}
		}
	}

	public String sendMessage(String msg) {
		final Map<String, String> data = new HashMap<String, String>();
		try {
			data.put("sessionid", URLDecoder.decode(sessionId, "UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		data.put("message", msg);
		data.put("logpos", "" + logpos);
		data.put("version", "" + version);
		return fetch(baseTradeURL + "chat", "POST", data);
	}

	public void addItem(long itemid, int slot) {
		final Map<String, String> data = new HashMap<String, String>();
		try {
			data.put("sessionid", URLDecoder.decode(sessionId, "UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		data.put("appid", "440");
		data.put("contextid", "2");
		data.put("itemid", "" + itemid);
		data.put("slot", "" + slot);
		fetch(baseTradeURL + "additem", "POST", data);
	}

	public void removeItem(long itemid) {
		final Map<String, String> data = new HashMap<String, String>();
		try {
			data.put("sessionid", URLDecoder.decode(sessionId, "UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		data.put("appid", "440");
		data.put("contextid", "2");
		data.put("itemid", "" + itemid);
		fetch(baseTradeURL + "removeitem", "POST", data);
	}

	public boolean setReady(boolean ready) {
		final Map<String, String> data = new HashMap<String, String>();
		try {
			data.put("sessionid", URLDecoder.decode(sessionId, "UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		data.put("ready", ready ? "true" : "false");
		data.put("version", "" + version);
		final String response = fetch(baseTradeURL + "toggleready", "POST", data);
		try {
			StatusObj status = new StatusObj((JSONObject) new JSONParser().parse(response));
			if (status.success) {
				if (status.trade_status == 0) {
					otherReady = status.them.ready;
					meReady = status.me.ready;
				} else {
					meReady = true;
				}
				return meReady;
			}
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	public JSONObject acceptTrade() throws ParseException {
		final Map<String, String> data = new HashMap<String, String>();
		try {
			data.put("sessionid", URLDecoder.decode(sessionId, "UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		data.put("version", "" + version);
		final String response = fetch(baseTradeURL + "confirm", "POST", data);

		return (JSONObject) new JSONParser().parse(response);
	}

	protected StatusObj getStatus() throws ParseException {
		final Map<String, String> data = new HashMap<String, String>();
		try {
			data.put("sessionid", URLDecoder.decode(sessionId, "UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		data.put("logpos", "" + logpos);
		data.put("version", "" + version);

		final String response = fetch(baseTradeURL + "tradestatus", "POST", data);

		return new StatusObj((JSONObject) new JSONParser().parse(response));
	}

	protected TradeInventory getInventory(SteamID steamid) {
		final String url = String.format("http://steamcommunity.com/profiles/%d/inventory/json/440/2/?trading=1", steamid.convertToLong());

		try {
			final String response = fetch(url, "GET", null, false);
			return new TradeInventory((JSONObject) new JSONParser().parse(response));
		} catch (final Exception e) {
			e.printStackTrace();
			return new TradeInventory();
		}
	}

	protected String fetch(String url, String method, Map<String, String> data) {
		return fetch(url, method, data, true);
	}

	protected String fetch(String url, String method, Map<String, String> data, boolean sendLoginData) {
		String cookies = "";
		if (sendLoginData) {
			cookies = "sessionid=" + sessionId + ";steamLogin=" + steamLogin;
		}
		final String response = SteamWeb.request(url, method, data, cookies);
		return response;
	}
}
