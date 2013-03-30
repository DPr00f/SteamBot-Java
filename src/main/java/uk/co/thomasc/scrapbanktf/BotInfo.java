package uk.co.thomasc.scrapbanktf;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class BotInfo {

	@Getter private final String username;

	@Getter private final String password;

	@Getter private final int id;

	@Getter private final String displayName;

	@Getter @Setter private static List<Long> admins;

	@Getter @Setter private static String apiKey;

	public BotInfo(Map<String, Object> info) {
		username = (String) info.get("username");
		password = (String) info.get("password");

		id = (int) info.get("id");

		displayName = (String) info.get("displayname");
	}

}
