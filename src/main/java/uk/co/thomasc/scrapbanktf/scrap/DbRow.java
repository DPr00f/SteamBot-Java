package uk.co.thomasc.scrapbanktf.scrap;

import java.util.Date;

import lombok.Getter;

import uk.co.thomasc.steamkit.types.steamid.SteamID;

public class DbRow {
	@Getter private final int rowId;
	@Getter private final SteamID steamId;
	private byte tradeAttempts = 0;
	private Date timeImportant = new Date();

	public DbRow(int rowId, SteamID steamid) {
		this.rowId = rowId;
		steamId = steamid;
	}

	public int secondsSince() {
		return (int) ((new Date().getTime() - timeImportant.getTime()) / 1000);
	}

	public void added() {
		timeImportant = new Date();
	}

	public byte incAttempts() {
		return ++tradeAttempts;
	}
}
