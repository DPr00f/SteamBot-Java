package uk.co.thomasc.scrapbanktf;

import org.json.simple.JSONObject;

public class SteamResult {
	public boolean success;

	public String message;

	public boolean captcha_needed;

	public String captcha_gid;

	public SteamResult(JSONObject obj) {
		success = (boolean) obj.get("success");
		message = (String) obj.get("message");
		captcha_needed = (boolean) obj.get("captcha_needed");
		captcha_gid = (String) obj.get("captcha_gid");
	}
}
