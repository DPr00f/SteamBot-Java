package uk.co.thomasc.scrapbanktf.command;

import uk.co.thomasc.scrapbanktf.Bot;
import uk.co.thomasc.steamkit.types.steamid.SteamID;

public abstract class Command {

	public final String call(Bot bot, SteamID steamid, String[] args, String argsStr) {
		return run(new CommandInfo(bot, steamid, args, argsStr));
	}

	public String run(CommandInfo cmdInfo) {
		return "";
	}

}
