package uk.co.thomasc.scrapbanktf;

import org.json.simple.JSONObject;

public class GetRsaKey {
	public boolean success;

	public String publickey_mod;

	public String publickey_exp;

	public String timestamp;

	public GetRsaKey(JSONObject obj) {
		success = (boolean) obj.get("success");
	}
}
