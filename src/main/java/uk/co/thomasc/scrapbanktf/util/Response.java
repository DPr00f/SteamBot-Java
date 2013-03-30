package uk.co.thomasc.scrapbanktf.util;

public class Response {

	private final long loaded = System.currentTimeMillis();
	private final String response;

	public Response(String response) {
		this.response = response;
	}

	public boolean isRecent() {
		return System.currentTimeMillis() - loaded < 1000 * 60 * 30; // 1000 milliseconds, 60 seconds, 30 minutes
	}

	public String getResponse() {
		return response;
	}

}
