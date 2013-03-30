package uk.co.thomasc.scrapbanktf;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import sun.misc.BASE64Encoder;

import uk.co.thomasc.scrapbanktf.util.Util;
import uk.co.thomasc.steamkit.util.crypto.RSACrypto;

@SuppressWarnings("restriction")
public class SteamWeb {

	public static String request(String url, String method, Map<String, String> data, String cookies) {
		return SteamWeb.request(url, method, data, cookies, true);
	}

	public static String request(String url, String method, Map<String, String> data, String cookies, boolean ajax) {
		String out = "";
		try {
			String dataString = "";
			if (data != null) {
				for (final String key : data.keySet()) {
					dataString += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(data.get(key), "UTF-8") + "&";
				}
			}
			if (!method.equals("POST")) {
				url += "?" + dataString;
			}
			final URL url2 = new URL(url);
			final HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
			conn.setRequestProperty("Cookie", cookies);
			conn.setRequestMethod(method);
			System.setProperty("http.agent", "");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
			conn.setRequestProperty("Host", "steamcommunity.com");
			conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestProperty("Accept", "text/javascript, text/hml, application/xml, text/xml, */*");
			conn.setRequestProperty("Referer", "http://steamcommunity.com/trade/1");

			if (ajax) {
				conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
				conn.setRequestProperty("X-Prototype-Version", "1.7");
			}

			if (method.equals("POST")) {
				conn.setDoOutput(true);
				final OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
				os.write(dataString);
				os.flush();
			}

			//cookies = conn.getHeaderField("Set-Cookie");
			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				if (out.length() > 0) {
					out += "\n";
				}
				out += line;
			}
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return out;
	}

	/**
	 * Executes the login by using the Steam Website.
	 */
	public static String doLogin(String username, String password) {
		try {
			Map<String, String> data = new HashMap<String, String>();
			data.put("username", username);
			final String response = SteamWeb.request("https://steamcommunity.com/login/getrsakey", "POST", data, null, false);
			final GetRsaKey rsaJSON = new GetRsaKey((JSONObject) new JSONParser().parse(response));

			// Validate
			if (rsaJSON.success != true) {
				return null;
			}

			//RSA Encryption
			final RSACrypto rsa = new RSACrypto(new BigInteger(1, SteamWeb.hexToByte(rsaJSON.publickey_exp)), new BigInteger(1, SteamWeb.hexToByte(rsaJSON.publickey_mod)), false);

			final byte[] encodedPassword = rsa.encrypt(password.getBytes());
			final String encryptedBase64Password = new BASE64Encoder().encode(encodedPassword);

			SteamResult loginJson = null;
			//String cookies;
			do {
				Util.printConsole("SteamWeb: Logging In...");

				final boolean captcha = loginJson != null && loginJson.captcha_needed == true;

				String time = "";
				String capGID = "";
				try {
					time = URLEncoder.encode(rsaJSON.timestamp, "UTF-8");
					capGID = loginJson == null ? null : URLEncoder.encode(loginJson.captcha_gid, "UTF-8");
				} catch (final UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				data = new HashMap<String, String>();
				data.put("password", encryptedBase64Password);
				data.put("username", username);
				data.put("emailauth", "");

				// Captcha
				String capText = "";
				if (captcha) {
					Util.printConsole("SteamWeb: Captcha is needed.");
					final Desktop desktop = java.awt.Desktop.getDesktop();
					if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
						try {
							desktop.browse(new URI("https://steamcommunity.com/public/captcha.php?gid=" + loginJson.captcha_gid));
						} catch (IOException | URISyntaxException e) {
							e.printStackTrace();
						}

						final InputStreamReader isr = new InputStreamReader(System.in);
						final BufferedReader br = new BufferedReader(isr);
						Util.printConsole("SteamWeb: Type the captcha:");
						try {
							capText = URLEncoder.encode(br.readLine(), "UTF-8");
						} catch (final IOException e) {
							e.printStackTrace();
						}
					} else {
						Util.printConsole("Desktop not supported");
						return null;
					}
				}

				data.put("captcha_gid", captcha ? capGID : "");
				data.put("captcha_text", captcha ? capText : "");
				// Captcha end

				data.put("emailsteamid", "");
				data.put("rsatimestamp", time);

				final String webResponse = SteamWeb.request("https://steamcommunity.com/login/dologin/", "POST", data, null, false);
				try {
					loginJson = new SteamResult((JSONObject) new JSONParser().parse(webResponse));
				} catch (final ParseException e) {
					e.printStackTrace();
				}

				//cookies = webResponse.cookies;
			} while (loginJson.captcha_needed == true);

			if (loginJson.success == true) {
				//submitCookies(cookies);
				//return cookies;
			} else {
				Util.printConsole("SteamWeb Error: " + loginJson.message);
			}
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	static void submitCookies(String cookies) {
		try {
			final URL url2 = new URL("https://steamcommunity.com/");
			final HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
			conn.connect();
			conn.setRequestProperty("Cookie", cookies);
			conn.setRequestMethod("POST");
			System.setProperty("http.agent", "");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
			conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");

			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			reader.readLine();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	static byte[] hexToByte(String hex) {
		if (hex.length() % 2 == 1) {
			Util.printConsole("The binary key cannot have an odd number of digits");
			return new byte[0];
		}

		final byte[] arr = new byte[hex.length() >> 1];
		final int l = hex.length();

		for (int i = 0; i < l >> 1; ++i) {
			arr[i] = (byte) ((SteamWeb.getHexVal(hex.charAt(i << 1)) << 4) + SteamWeb.getHexVal(hex.charAt((i << 1) + 1)));
		}

		return arr;
	}

	static int getHexVal(char hex) {
		final int val = hex;
		return val - (val < 58 ? 48 : 55);
	}

	//public static bool ValidateRemoteCertificate(object sender, X509Certificate certificate, X509Chain chain, SslPolicyErrors policyErrors) {
	// allow all certificates
	//return true;
	//}
}
