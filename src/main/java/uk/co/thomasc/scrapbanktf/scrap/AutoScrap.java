package uk.co.thomasc.scrapbanktf.scrap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.thomasc.scrapbanktf.Bot;
import uk.co.thomasc.scrapbanktf.Main;
import uk.co.thomasc.scrapbanktf.inventory.Inventory;
import uk.co.thomasc.scrapbanktf.inventory.Item;
import uk.co.thomasc.scrapbanktf.util.ConsoleColor;
import uk.co.thomasc.scrapbanktf.util.MutableInt;
import uk.co.thomasc.scrapbanktf.util.Util;
import uk.co.thomasc.steamkit.base.ClientMsgProtobuf;
import uk.co.thomasc.steamkit.base.gc.tf2.ECraftingRecipe;
import uk.co.thomasc.steamkit.base.generated.SteammessagesClientserver.CMsgClientGamesPlayed;
import uk.co.thomasc.steamkit.base.generated.SteammessagesClientserver.CMsgClientGamesPlayed.GamePlayed;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EMsg;
import uk.co.thomasc.steamkit.steam3.handlers.steamgamecoordinator.callbacks.CraftResponseCallback;

public class AutoScrap {

	private final Bot bot;
	private Boolean ingame = false;
	private static Object lck = new Object();
	private Map<Integer, List<Long>> metal = new HashMap<Integer, List<Long>>();

	public AutoScrap(Bot bot) {
		this.bot = bot;
	}

	private void opengame() {
		final ClientMsgProtobuf<CMsgClientGamesPlayed.Builder> playGame = new ClientMsgProtobuf<CMsgClientGamesPlayed.Builder>(CMsgClientGamesPlayed.class, EMsg.ClientGamesPlayed);

		playGame.getBody().addGamesPlayed(GamePlayed.newBuilder().setGameId(440).build());

		bot.steamClient.send(playGame);
	}

	public void onWelcome() {
		ingame = true;
		synchronized (this) {
			notify();
		}
	}
	
	private CraftResponseCallback response;
	private Object craftWait = new Object();
	
	public void onCraft(CraftResponseCallback response) {
		this.response = response;
		synchronized (craftWait) {
			craftWait.notify();
		}
	}

	private CraftResponseCallback scrap(long item1, long item2) {
		return craft(ECraftingRecipe.SmeltClassWeapons, item1, item2);
	}
	
	private CraftResponseCallback craft(ECraftingRecipe recipe, long... items) {
		if (!ingame) {
			opengame();
			try {
				synchronized (this) {
					wait();
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		bot.steamGC.craft(recipe, items);
		try {
			synchronized (craftWait) {
				craftWait.wait(30000);
			}
			return response;
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void combine() {
		if (metal.get(1).size() < 2 && metal.get(2).size() > 0) { // We need more reclaimed!
			CraftResponseCallback res = craft(ECraftingRecipe.SmeltRefined, metal.get(2).remove(0));
			if (res != null) {
				for (Long item : res.getItems()) {
					metal.get(1).add(item);
				}
			}
		}
		if (metal.get(0).size() < 2 && metal.get(1).size() > 0) { // We need more scrap!
			CraftResponseCallback res = craft(ECraftingRecipe.SmeltReclaimed, metal.get(1).remove(0));
			if (res != null) {
				for (Long item : res.getItems()) {
					metal.get(0).add(item);
				}
			}
		}
		
		while (metal.get(0).size() > 4) {
			CraftResponseCallback res = craft(ECraftingRecipe.CombineScrap, metal.get(0).remove(0), metal.get(0).remove(0), metal.get(0).remove(0));
			if (res != null) {
				for (Long item : res.getItems()) { // Should only be one, but who knows :P
					metal.get(1).add(item);
				}
			}
		}
		while (metal.get(1).size() > 4) {
			CraftResponseCallback res = craft(ECraftingRecipe.CombineReclaimed, metal.get(1).remove(0), metal.get(1).remove(0), metal.get(1).remove(0));
			if (res != null) {
				for (Long item : res.getItems()) { // Should only be one, but who knows :P
					metal.get(2).add(item);
				}
			}
		}
	}

	public void run() {
		synchronized (AutoScrap.lck) {
			final Inventory MyInventory = Inventory.fetchInventory(bot.steamClient.getSteamId().convertToLong(), false);
			if (MyInventory == null) {
				Util.printConsole("Could not fetch own inventory via Steam API! (AutoScrap)", bot, ConsoleColor.White, true);
			}
			
			metal.put(0, new ArrayList<Long>());
			metal.put(1, new ArrayList<Long>());
			metal.put(2, new ArrayList<Long>());

			//final ResultSet result = Main.sql.selectQuery("SELECT schemaid, classid, (stock - COUNT(reservation.Id) + IF(highvalue=1 and stock - COUNT(reservation.Id) > 1,-2,0)) as stk FROM items LEFT JOIN reservation ON items.schemaid = reservation.itemid WHERE highvalue != 2 GROUP BY items.schemaid HAVING stk > 4");
			final Map<Integer, MutableInt> count = new HashMap<Integer, MutableInt>();
			final Map<Integer, Byte> classid = new HashMap<Integer, Byte>();
			final Map<Integer, MutableInt> scraped = new HashMap<Integer, MutableInt>();
			/*try {
				while (result.next()) {
					try {
						count.put(result.getInt("schemaid"), new MutableInt(result.getInt("stk")));
						classid.put(result.getInt("schemaid"), result.getByte("classid"));
						scraped.put(result.getInt("schemaid"), new MutableInt(0));
					} catch (final SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (final SQLException e) {
				e.printStackTrace();
			}*/
			final Map<Byte, Long> otherid = new HashMap<Byte, Long>();

			for (final Long id : MyInventory.getItemIds()) {
				final Item item = MyInventory.getItem(id);

				if (count.containsKey(item.defIndex) && count.get(item.defIndex).get() > 4) {
					count.get(item.defIndex).decrement();
					if (otherid.containsKey(classid.get(item.defIndex))) {
						final Item item2 = MyInventory.getItem(otherid.get(classid.get(item.defIndex)));
						CraftResponseCallback callback = scrap(otherid.get(classid.get(item.defIndex)), id);
						if (callback != null) {
							for (long itemId : callback.getItems()) {
								metal.get(0).add(itemId);
							}
						}
						scraped.get(item.defIndex).increment();
						scraped.get(item2.defIndex).increment();
						otherid.remove(classid.get(item.defIndex));
					} else {
						otherid.put(classid.get(item.defIndex), id);
					}
				} else if (item.defIndex >= 5000 && item.defIndex <= 5002) {
					metal.get(item.defIndex - 5000).add(item.id);
				}
			}

			combine();

			int totalItems = 0;
			for (final int id : scraped.keySet()) {
				if (scraped.get(id).get() > 0) {
					totalItems += scraped.get(id).get();
					//Main.sql.update("UPDATE items SET stock = stock - " + scraped.get(id).get() + " WHERE schemaid = " + id);
				}
			}
			//Main.sql.update("UPDATE bots SET items = items - " + totalItems + ", scrap = scrap + " + totalItems / 2 + " WHERE botid = " + bot.getBotId());

			if (ingame) {
				final ClientMsgProtobuf<CMsgClientGamesPlayed.Builder> playGame = new ClientMsgProtobuf<CMsgClientGamesPlayed.Builder>(CMsgClientGamesPlayed.class, EMsg.ClientGamesPlayed);
				bot.steamClient.send(playGame);
				ingame = false;
			}
		}
	}
}
