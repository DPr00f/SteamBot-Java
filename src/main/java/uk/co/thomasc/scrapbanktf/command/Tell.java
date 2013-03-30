package uk.co.thomasc.scrapbanktf.command;

import uk.co.thomasc.scrapbanktf.util.Util;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EChatEntryType;
import uk.co.thomasc.steamkit.types.steamid.SteamID;

public class Tell extends Command {

	@Override
	public String run(CommandInfo cmdInfo) {
		final String msg = Util.removeArg0(cmdInfo.getArgsStr());

		for (final SteamID f : cmdInfo.getBot().steamFriends.getFriendList()) {
			if (cmdInfo.getBot().steamFriends.getFriendPersonaName(f).equalsIgnoreCase(cmdInfo.getArgs()[0])) {
				cmdInfo.getBot().steamFriends.sendChatMessage(f, EChatEntryType.ChatMsg, cmdInfo.getBot().steamFriends.getPersonaName() + " says '" + msg + "'");
				return "Message Sent!";
			}
		}

		return "Could not find user :/";
	}

}
