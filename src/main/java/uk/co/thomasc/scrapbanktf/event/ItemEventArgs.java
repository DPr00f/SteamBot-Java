package uk.co.thomasc.scrapbanktf.event;

import lombok.Getter;

import uk.co.thomasc.scrapbanktf.inventory.Item;
import uk.co.thomasc.scrapbanktf.util.ItemInfo;
import uk.co.thomasc.steamkit.util.cSharp.events.EventArgs;

public class ItemEventArgs extends EventArgs {
	@Getter private final ItemInfo schemaItem;
	@Getter private final Item inventoryItem;

	public ItemEventArgs(ItemInfo data, Item endPoint) {
		schemaItem = data;
		inventoryItem = endPoint;
	}
}
