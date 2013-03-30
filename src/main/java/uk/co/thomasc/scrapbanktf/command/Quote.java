package uk.co.thomasc.scrapbanktf.command;

import java.sql.ResultSet;
import java.sql.SQLException;

import uk.co.thomasc.scrapbanktf.Main;
import uk.co.thomasc.scrapbanktf.util.Util;

public class Quote extends Command {

	@Override
	public String run(CommandInfo cmdInfo) {
		if (cmdInfo.getArg(0).equalsIgnoreCase("add")) {
			final String quote = Util.removeArg0(cmdInfo.getArgsStr());
			//Main.sql.query("INSERT INTO sayings (message) VALUES ('" + quote + "')");
			return "Quote added :D";
		} else {
			/*final ResultSet rs = Main.sql.selectQuery("SELECT message FROM sayings ORDER BY RAND() LIMIT 1");
			try {
				if (rs.first()) {
					return rs.getString("message");
				} else {
					return "Woops, I canne finda de quotes";
				}
			} catch (final SQLException e) {
				e.printStackTrace();
			}*/
                    return "I'm in SPAAAAAAAAAAAAACE! ...and there are no quotes in space :<";
		}
	}

}
