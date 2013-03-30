package uk.co.thomasc.scrapbanktf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import uk.co.thomasc.scrapbanktf.util.ConsoleColor;
import uk.co.thomasc.scrapbanktf.util.SQL;
import uk.co.thomasc.scrapbanktf.util.Util;
import uk.co.thomasc.steamkit.types.steamid.SteamID;

public class Main {

	public static List<SteamID> bots = new ArrayList<SteamID>();

	public static Map<String, Object> yml;

	public static void main(String[] args) {
		new Main();
	}

	//public static SQL sql = new SQL("jdbc:mysql://meow");

	@SuppressWarnings("unchecked")
	public Main() {
		final Yaml yaml = new Yaml();
		Main.yml = (Map<String, Object>) yaml.load(this.getClass().getResourceAsStream("/settings.yaml"));

		BotInfo.setAdmins((List<Long>) Main.yml.get("admins"));
		BotInfo.setApiKey((String) Main.yml.get("apikey"));

		byte counter = 0;
		for (final Map<String, Object> info : (List<Map<String, Object>>) Main.yml.get("bots")) {
			Util.printConsole("Launching bot " + counter++);
			new Thread(new BotThread(new BotInfo(info))).start();
			try {
				Thread.sleep(5000);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public class BotThread implements Runnable {

		private final BotInfo info;

		public BotThread(BotInfo info) {
			this.info = info;
		}

		@Override
		public void run() {
			int crashes = 0;
			while (crashes < 1000) {
				try {
					new Bot(info);
				} catch (final Exception e) {
					String error = "Unhandled error on bot " +  crashes++ + "\n" + e.getClass() + " at";
					for (StackTraceElement el : e.getStackTrace()) {
						error += "\n" + el;
					}
					Util.printConsole(error, info.getId(), ConsoleColor.White, true);
				}
			}
		}

	}

}
