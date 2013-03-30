package uk.co.thomasc.scrapbanktf.scrap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import uk.co.thomasc.scrapbanktf.Bot;
import uk.co.thomasc.scrapbanktf.BotInfo;
import uk.co.thomasc.scrapbanktf.Main;
import uk.co.thomasc.scrapbanktf.inventory.Inventory;
import uk.co.thomasc.scrapbanktf.util.ConsoleColor;
import uk.co.thomasc.scrapbanktf.util.Util;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EFriendRelationship;
import uk.co.thomasc.steamkit.types.steamid.SteamID;

@SuppressWarnings("unchecked")
public class QueueHandler implements Runnable {
	private final List<DbRow> users = new ArrayList<DbRow>();
	private final Queue<DbRow> tradeQueue = new LinkedList<DbRow>();
	public DbRow currentTrader;
	public boolean canTrade = false;
	private final ArrayList<Integer> reserved = new ArrayList<Integer>();
	private final Bot bot;
	private boolean started = false;

	public static List<Bot> needItems = new ArrayList<Bot>();
	public List<Integer> neededItems = new ArrayList<Integer>();
	public boolean needItemsBool = false;
	public boolean gotItems = false;

	public AutoScrap autoScrap;

	public QueueHandler(Bot bot) {
		this.bot = bot;
		autoScrap = new AutoScrap(bot);
		//Main.sql.update("UPDATE queue SET queued = '-1' WHERE queued = '" + bot.getBotId() + "'");
	}

	public void start() {
		if (!started) {
			final Thread oThread = new Thread(this);
			oThread.start();
			started = true;
		}
	}

	@Override
	public void run() {
		autoScrap.run();
		while (true) {
			try {
				Thread.sleep(5000);
			} catch (final InterruptedException e1) {
				e1.printStackTrace();
			}
			if (users.size() < 5) {
				/*try {
					final ResultSet reader = Main.sql.selectQuery("SELECT Id, steamid FROM queue WHERE (botid = -1 || botid = " + bot.getBotId() + ") && queued = -1 ORDER BY Id LIMIT 1");
					if (reader.next()) {
						final int id = reader.getInt("Id");
						if (Main.sql.update("UPDATE queue SET queued = '" + bot.getBotId() + "' WHERE Id = '" + id + "' and queued = -1") > 0) {
							final SteamID other = new SteamID(reader.getLong("steamid"));
							//do {
							if (bot.steamFriends.getFriendRelationship(other) == EFriendRelationship.Friend) {
								tradeQueue.add(new DbRow(id, other));
							} else {
								bot.steamFriends.addFriend(other);
								users.add(new DbRow(id, other));
							}
							//} while (bot.SteamFriends.GetFriendRelationship(other) == EFriendRelationship.None);
						}
					}
				} catch (final SQLException e) {
					Util.printConsole(e.getMessage(), bot, ConsoleColor.White);
				}*/
			}

			final List<DbRow> toremove = new ArrayList<DbRow>();
			for (final DbRow row : users) {
				if (row.secondsSince() > 60 * 5) {
					bot.steamFriends.removeFriend(row.getSteamId());
					toremove.add(row);
					//Main.sql.update("DELETE FROM queue WHERE Id = '" + row.getRowId() + "'");
				}
			}
			users.removeAll(toremove);

			if (canTrade && (currentTrader == null || needItemsBool)) {
				Bot toRemove = null;
				for (final Bot bot : QueueHandler.needItems) {
					if (bot != this.bot) {
						final List<Integer> MyInventory = Inventory.fetchInventory(this.bot.steamClient.getSteamId().convertToLong(), false).getItems();
						final List<Integer> toTrade = new ArrayList<Integer>();
						for (final int itemid : bot.queueHandler.neededItems) {
							if (MyInventory.contains(itemid)) {
								toTrade.add(itemid);
								MyInventory.remove((Integer) itemid);
							}
						}
						for (final int itemid : toTrade) {
							bot.queueHandler.neededItems.remove((Integer) itemid);
						}
						if (toTrade.size() > 0) {
							toRemove = bot;
							currentTrader = new DbRow(0, null);
							this.bot.toTrade = toTrade;
							bot.queueHandler.autoScrap.run();
							this.bot.steamTrade.trade(bot.steamClient.getSteamId());
							break;
						}
					}
				}
				if (toRemove != null) {
					QueueHandler.needItems.remove(toRemove);
				}
			}

			if (gotItems) {
				autoScrap.run();
				Util.printConsole("Items acquired, sending trade request", bot, ConsoleColor.Yellow);
				bot.steamTrade.trade(currentTrader.getSteamId());
				gotItems = false;
			}

			if (canTrade && tradeQueue.size() > 0 && currentTrader == null) {
				autoScrap.run();
				try {
					Util.printConsole("Fetching next user from queue", bot, ConsoleColor.Yellow);
					currentTrader = tradeQueue.poll();

					final List<Integer> MyInventory = Inventory.fetchInventory(bot.steamClient.getSteamId().convertToLong(), false).getItems();
					//final ResultSet reader = Main.sql.selectQuery("SELECT itemid FROM reservation WHERE steamid = '" + currentTrader.getSteamId().convertToLong() + "'");
					reserved.clear();
					//while (reader.next()) {
					//	reserved.add(reader.getInt("itemid"));
					//}
					final List<Integer> reservedTmp = (List<Integer>) reserved.clone();
					for (final int itemid : MyInventory) {
						reservedTmp.remove((Integer) itemid);
					}

					if (reservedTmp.size() > 0) {
						Util.printConsole("Need to acquire " + reservedTmp.size() + " items before trading", bot, ConsoleColor.Yellow);
						//We don't have all the items we need... :/
						neededItems = reservedTmp;
						QueueHandler.needItems.add(bot);
						needItemsBool = true;
					} else {
						Util.printConsole("Sending new trade request", bot, ConsoleColor.Yellow);
						bot.steamTrade.trade(currentTrader.getSteamId());
					}
				}/* catch (final SQLException e) {
					Util.printConsole(e.getMessage(), bot, ConsoleColor.White, true);
					if (currentTrader != null) {
						tradeQueue.add(currentTrader);
						currentTrader = null;
					}
				}*/ catch (final Exception e) {
					tradeEnded();
					Util.printConsole("EEEK" + e.getMessage(), bot, ConsoleColor.White, true);
					for (final StackTraceElement el : e.getStackTrace()) {
						Util.printConsole("EEEK" + el.toString(), bot, ConsoleColor.White, true);
					}
					//throw e;
				}
			}
		}
	}

	public void reQueue() {
		tradeQueue.add(currentTrader);
		needItemsBool = false;
		neededItems.clear();
		QueueHandler.needItems.remove(bot);
		currentTrader = null;
	}

	public void tradeEnded() {
		if (currentTrader != null) {
			Util.printConsole("Trade ended", bot, ConsoleColor.Yellow);
			if (!Main.bots.contains(currentTrader.getSteamId()) && !BotInfo.getAdmins().contains(currentTrader.getSteamId().convertToLong())) {
				bot.steamFriends.removeFriend(currentTrader.getSteamId());
			}
			//Main.sql.update("DELETE FROM queue WHERE Id = '" + currentTrader.getRowId() + "'");
			currentTrader = null;
		}
	}

	public void acceptedRequest(SteamID steamid) {
		final List<DbRow> toremove = new ArrayList<DbRow>();
		for (final DbRow row : users) {
			if (row.getSteamId().equals(steamid)) {
				toremove.add(row);
				tradeQueue.add(row);
			}
		}
		users.removeAll(toremove);
	}

	public void ignoredTrade(SteamID steamID) {
		if (currentTrader != null && steamID.equals(currentTrader.getSteamId())) {
			if (currentTrader.incAttempts() < 3) {
				bot.steamTrade.trade(currentTrader.getSteamId());
			} else {
				tradeEnded();
			}
		}
	}

	public List<Integer> getReservedItems() {
		return (List<Integer>) reserved.clone();
	}
}
