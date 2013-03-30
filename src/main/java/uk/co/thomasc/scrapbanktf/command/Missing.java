package uk.co.thomasc.scrapbanktf.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.co.thomasc.scrapbanktf.inventory.Inventory;
import uk.co.thomasc.scrapbanktf.inventory.Item;
import uk.co.thomasc.scrapbanktf.util.Util;

public class Missing extends Command {

	private static int[] essentialWeapons = { 450, 46, 325, 317, 163, 355, 45, 221, 222, 44, 220, 448, 349, 449, 648, 226, 228, 129, 354, 441, 127, 447, 128, 133, 414, 444, 416, 442, 237, 38, 326, 40, 215, 351, 39, 153, 595, 594, 214, 348, 593, 405, 131, 327, 132, 308, 404, 172, 130, 406, 265, 307, 312, 311, 159, 426, 425, 331, 239, 656, 43, 41, 42, 424, 310, 589, 141, 142, 329, 588, 528, 155, 527, 140, 304, 36, 305, 35, 412, 411, 413, 37, 173, 402, 232, 642, 231, 56, 58, 526, 57, 401, 230, 171, 61, 461, 60, 356, 59, 525, 460, 224, 649, 225, 357, 154, 415 };
	private static int[] alternativeWeapons = { 660, 669, 452, 572, 658, 513, 659, 466, 457, 608, 661, 266, 482, 609, 587, 654, 433, 298, 662, 169, 663, 664, 161, 727, 297, 665, 638, 574, 474, 264, 294, 423 };
	private static int[] defaultWeapons = { 190, 200, 205, 196, 208, 192, 191, 206, 207, 202, 195, 197, 198, 211, 204, 193, 203, 201, 212, 210, 194, 199, 209 };
	private static Map<String, int[]> lists = new HashMap<String, int[]>();

	static {
		Missing.lists.put("e", Missing.essentialWeapons);
		Missing.lists.put("a", Missing.alternativeWeapons);
		Missing.lists.put("d", Missing.defaultWeapons);
	}

	@Override
	public String run(CommandInfo cmdInfo) {
		final List<Integer> weapons = new ArrayList<Integer>();
		final String include = cmdInfo.getArg(0, "e");
		for (final Entry<String, int[]> entry : Missing.lists.entrySet()) {
			if (include.contains(entry.getKey())) {
				for (final int i : entry.getValue()) {
					weapons.add(i);
				}
			}
		}

		final Inventory inv = Inventory.fetchInventory(cmdInfo.getSteamid().convertToLong());
		for (final Item item : inv.items) {
			weapons.remove((Integer) item.defIndex);
		}

		String out = "Woah, you sir are a god among gigs\nYou have all essential items :D";
		if (weapons.size() > 0) {
			out = "You are missing these items:";

			for (final int it : weapons) {
				out += "\n" + Util.getItemInfo(it).getName();
			}
		}

		return out;
	}

}
