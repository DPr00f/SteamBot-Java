package uk.co.thomasc.scrapbanktf.command;

import uk.co.thomasc.scrapbanktf.Bot;
import uk.co.thomasc.steamkit.types.steamid.SteamID;

public class CommandInfo {

	private final Bot bot;
	private final SteamID steamid;
	private final String[] args;
	private final String argsStr;

	public CommandInfo(Bot bot, SteamID steamid, String[] args, String argsStr) {
		this.steamid = steamid;
		this.args = args;
		this.argsStr = argsStr;
		this.bot = bot;
	}

	public SteamID getSteamid() {
		return steamid;
	}

	public String getArg(int index) {
		return getArg(index, "");
	}

	public String getArg(int index, String def) {
		return argsStr.length() <= 0 ? def : args.length > index ? args[index] : def;
	}

	public int getArg(int index, int def) {
		return argsStr.length() <= 0 ? def : args.length > index ? Integer.parseInt(args[index]) : def;
	}

	public String[] getArgs() {
		return args;
	}

	public String getArgsStr() {
		return argsStr;
	}

	public Bot getBot() {
		return bot;
	}
}
