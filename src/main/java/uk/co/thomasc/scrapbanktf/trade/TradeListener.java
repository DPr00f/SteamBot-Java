package uk.co.thomasc.scrapbanktf.trade;

import uk.co.thomasc.scrapbanktf.Bot;
import uk.co.thomasc.scrapbanktf.inventory.Item;
import uk.co.thomasc.scrapbanktf.util.ItemInfo;

public abstract class TradeListener {

	protected int slot = 0;
	protected final Bot bot;

	public Trade trade;

	public abstract void onError(int eid);

	public abstract void onTimeout();

	public abstract void onAfterInit();

	public abstract void onUserAddItem(ItemInfo schemaItem, Item inventoryItem);

	public abstract void onUserRemoveItem(ItemInfo schemaItem, Item inventoryItem);

	public abstract void onMessage(String msg);

	public abstract void onUserSetReadyState(boolean ready);

	public abstract void onUserAccept();

	public abstract void onNewVersion();

	public abstract void onComplete();

	public TradeListener(Bot bot) {
		this.bot = bot;
	}
}
