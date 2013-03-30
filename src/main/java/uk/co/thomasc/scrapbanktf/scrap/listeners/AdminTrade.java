package uk.co.thomasc.scrapbanktf.scrap.listeners;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import uk.co.thomasc.scrapbanktf.Bot;
import uk.co.thomasc.scrapbanktf.Main;
import uk.co.thomasc.scrapbanktf.inventory.Item;
import uk.co.thomasc.scrapbanktf.trade.TradeListener;
import uk.co.thomasc.scrapbanktf.util.ConsoleColor;
import uk.co.thomasc.scrapbanktf.util.ItemInfo;
import uk.co.thomasc.scrapbanktf.util.MutableInt;
import uk.co.thomasc.scrapbanktf.util.Util;

public class AdminTrade extends TradeListener {
	private int scrapDiff = 0;
	private int itemDiff = 0;

	public AdminTrade(Bot bot) {
		super(bot);
	}

	@Override
	public void onTimeout() {
		Util.printConsole("Timeout during trade with admin", bot, ConsoleColor.Red, true);
	}

	@Override
	public void onError(int eid) {
		Util.printConsole("Error(" + eid + ") while trading with admin", bot, ConsoleColor.Red);
		bot.queueHandler.tradeEnded();
		bot.currentTrade = null;
	}

	@Override
	public void onAfterInit() {
		itemDiff = 0;
		scrapDiff = 0;

		//final ResultSet result = Main.sql.selectQuery("SELECT schemaid, stock - COUNT(reservation.Id) as stk FROM items LEFT JOIN reservation ON items.schemaid = reservation.itemid WHERE highvalue = 2 GROUP BY items.schemaid HAVING stk > 0");
		final Map<Integer, MutableInt> count = new HashMap<Integer, MutableInt>();
		/*try {
			while (result.next()) {
				count.put(result.getInt("schemaid"), new MutableInt(result.getInt("stk")));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}*/

		for (final long child : trade.MyItems.getIds()) {
			final Item item = trade.MyInventory.getItem(child);

			if (count.containsKey(item.defIndex) && count.get(item.defIndex).get() > 0) {
				count.get(item.defIndex).decrement();

				if (item.defIndex == 5000) {
					scrapDiff--;
				} else {
					itemDiff--;
				}
				trade.addItem(item.id, slot++);
			}
		}
	}

	@Override
	public void onUserAccept() {
		OnFinished();
	}

	@Override
	public void onUserSetReadyState(boolean ready) {
		trade.setReady(ready);
	}

	public void OnFinished() {
		try {
			final JSONObject js = trade.acceptTrade();
			if ((boolean) js.get("success") == true) {
				Util.printConsole("Success " + bot.steamClient.getSteamId(), bot, ConsoleColor.Yellow, true);
				return;
			}
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		Util.printConsole("Failure " + bot.steamClient.getSteamId(), bot, ConsoleColor.Yellow, true);
	}

	@Override
	public void onComplete() {
		for (final long child : trade.OtherTrade) {
			final Item record = trade.OtherInventory.getItem(child);
			if (record.defIndex != 5000) {
				//Main.sql.update("UPDATE items SET stock = stock + 1, `in` = `in` + 1 WHERE schemaid = '" + record.defIndex + "'");
			}
		}
		for (final long child : trade.MyTrade) {
			final Item record = trade.MyInventory.getItem(child);
			if (record.defIndex != 5000) {
				//Main.sql.update("UPDATE items SET stock = stock - 1 WHERE schemaid = '" + record.defIndex + "'");
			}
		}
		//Main.sql.update("UPDATE bots SET items = items + " + itemDiff + ", scrap = scrap + " + scrapDiff + " WHERE botid = '" + bot.getBotId() + "'");

		bot.queueHandler.tradeEnded();
		bot.currentTrade = null;
	}

	@Override
	public void onUserAddItem(ItemInfo schemaItem, Item invItem) {
		if (invItem.defIndex == 5000) {
			scrapDiff++;
		} else {
			itemDiff++;
		}
	}

	@Override
	public void onUserRemoveItem(ItemInfo schemaItem, Item invItem) {
		if (invItem.defIndex == 5000) {
			scrapDiff--;
		} else {
			itemDiff--;
		}
	}

	@Override
	public void onMessage(String message) {
		if (message.startsWith("scrap")) {
			int count = Integer.parseInt(message.substring(6));
			for (final long child : trade.MyItems.getIds()) {
				final Item item = trade.MyInventory.getItem(child);

				if (item.defIndex == 5000 && count > 0 && !trade.MyTrade.contains(item.id)) {
					count--;
					scrapDiff--;
					trade.addItem(item.id, slot++);
				}
			}
		}
	}

	@Override
	public void onNewVersion() {

	}
}
