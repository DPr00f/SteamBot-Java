package uk.co.thomasc.scrapbanktf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import sun.misc.BASE64Encoder;

import uk.co.thomasc.scrapbanktf.command.Command;
import uk.co.thomasc.scrapbanktf.command.Commands;
import uk.co.thomasc.scrapbanktf.scrap.QueueHandler;
import uk.co.thomasc.scrapbanktf.scrap.listeners.AdminTrade;
import uk.co.thomasc.scrapbanktf.scrap.listeners.ExchangeTrade;
import uk.co.thomasc.scrapbanktf.scrap.listeners.ScrapTrade;
import uk.co.thomasc.scrapbanktf.trade.Trade;
import uk.co.thomasc.scrapbanktf.trade.TradeListener;
import uk.co.thomasc.scrapbanktf.util.ConsoleColor;
import uk.co.thomasc.scrapbanktf.util.Util;
import uk.co.thomasc.steamkit.base.gc.EGCMsgBase;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EChatEntryType;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EEconTradeResponse;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EFriendRelationship;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EPersonaState;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EResult;
import uk.co.thomasc.steamkit.steam3.handlers.steamapps.callbacks.GameConnectTokensCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.SteamFriends;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.ChatMsgCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendMsgCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.PersonaStateCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamgamecoordinator.SteamGameCoordinator;
import uk.co.thomasc.steamkit.steam3.handlers.steamgamecoordinator.callbacks.CraftResponseCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamgamecoordinator.callbacks.MessageCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.SteamTrading;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.callbacks.SessionStartCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.callbacks.TradeProposedCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamtrading.callbacks.TradeResultCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.SteamUser;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOffCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoginKeyCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.UpdateMachineAuthCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.MachineAuthDetails;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.DisconnectedCallback;
import uk.co.thomasc.steamkit.steam3.webapi.WebAPI;
import uk.co.thomasc.steamkit.types.keyvalue.KeyValue;
import uk.co.thomasc.steamkit.types.steamid.SteamID;
import uk.co.thomasc.steamkit.util.KeyDictionary;
import uk.co.thomasc.steamkit.util.WebHelpers;
import uk.co.thomasc.steamkit.util.cSharp.events.ActionT;
import uk.co.thomasc.steamkit.util.crypto.CryptoHelper;
import uk.co.thomasc.steamkit.util.crypto.RSACrypto;

@SuppressWarnings("restriction")
public class Bot {
	public boolean isLoggedIn = false;

	@Getter private String displayName;
	public long[] admins;

	public SteamFriends steamFriends;
	public SteamClient steamClient;
	public SteamTrading steamTrade;
	public SteamGameCoordinator steamGC;
	public SteamUser steamUser;

	public QueueHandler queueHandler;

	public Trade currentTrade;

	BotInfo info;
	String sessionId;
	String token;
        private LogOnDetails logOnDetails;

	public List<Integer> toTrade = new ArrayList<Integer>();

	public Map<String, String> responses = new HashMap<String, String>();

	public Bot(BotInfo info) {
		this.info = info;

		steamClient = new SteamClient();
		steamTrade = steamClient.getHandler(SteamTrading.class);
		steamUser = steamClient.getHandler(SteamUser.class);
		steamFriends = steamClient.getHandler(SteamFriends.class);
		steamGC = steamClient.getHandler(SteamGameCoordinator.class);
		queueHandler = new QueueHandler(this);

                logOnDetails = new LogOnDetails().username(info.getUsername()).password(info.getPassword());
                
		steamClient.connect();
                
		while (true) {
			Update();
		}
	}

	public int getBotId() {
		return info.getId();
	}
        private CallbackMsg cbMsg;
        
	public void Update() {
		while (true) {
			cbMsg = steamClient.getCallback(true);
			if (cbMsg == null) {
				try {
					Thread.sleep(500);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				break;
			}

			handleSteamMessage(cbMsg);
		}

		if (currentTrade != null) {
			try {
				currentTrade.Poll();
			} catch (final Exception e) {
				String error = "Error polling the trade: " + e.getClass();
				for (StackTraceElement el : e.getStackTrace()) {
					error += "\n" + el;
				}
				Util.printConsole(error, this, ConsoleColor.White, true);
			}
		}
	}
	void handleSteamMessage(CallbackMsg msg) {
		msg.handle(ConnectedCallback.class, new ActionT<ConnectedCallback>() {
			@Override
			public void call(ConnectedCallback callback) {
				Util.printConsole("Connection Status " + callback.getResult(), Bot.this, ConsoleColor.Magenta);

				if (callback.getResult() == EResult.OK) {
                                    //logOnDetails.authCode("V97CG"); //You have to manually replace it.
                                    steamUser.logOn(logOnDetails);
            
				} else {
					Util.printConsole("Failed to Connect to the steam community", Bot.this, ConsoleColor.Red);
					steamClient.connect();
				}
                                
			}
		});
                
                msg.handle(UpdateMachineAuthCallback.class, new ActionT<UpdateMachineAuthCallback>(){

                    @Override
                    public void call(UpdateMachineAuthCallback obj) {
                        
                        Util.printConsole("Connection Status cheguei", Bot.this, ConsoleColor.Magenta);
                    }
                    
                });
                
                System.out.println(msg.getClass());

		msg.handle(LoggedOnCallback.class, new ActionT<LoggedOnCallback>() {
			@Override
			public void call(LoggedOnCallback callback) {
                                
				if (callback.getResult() != EResult.OK) {
					Util.printConsole("Login Failure: " + callback.getResult(), Bot.this, ConsoleColor.Red);
				}
                                
                                if(callback.getResult() == EResult.AccountLogonDenied){
                                    Util.printConsole("This account is protected by Steam Guard.  Enter the authentication code sent to the proper email: ", Bot.this, ConsoleColor.DarkRed);
                                    try{
                                        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                                        String s = bufferRead.readLine();
                                        logOnDetails.authCode(s);
                                    }
                                    catch(IOException e)
                                    {
                                            e.printStackTrace();
                                    }
                                }
                                
                                
                                if (callback.getResult() == EResult.InvalidLoginAuthCode)
                                {
                                    Util.printConsole("An Invalid Authorization Code was provided.  Enter the authentication code sent to the proper email: ", Bot.this, ConsoleColor.DarkRed);
                                    try{
                                        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                                        String s = bufferRead.readLine();
                                        logOnDetails.authCode(s);
                                    }
                                    catch(IOException e)
                                    {
                                            e.printStackTrace();
                                    }
                                }
			}
		});

		msg.handle(MessageCallback.class, new ActionT<MessageCallback>() {
			@Override
			public void call(MessageCallback callback) {
				if (callback.getEMsg() == EGCMsgBase.ClientWelcome) {
					queueHandler.autoScrap.onWelcome();
				}
			}
		});
		
		msg.handle(CraftResponseCallback.class, new ActionT<CraftResponseCallback>() {
			@Override
			public void call(CraftResponseCallback obj) {
				queueHandler.autoScrap.onCraft(obj);
			}
		});

		msg.handle(GameConnectTokensCallback.class, new ActionT<GameConnectTokensCallback>() {
			@Override
			public void call(GameConnectTokensCallback callback) {
				queueHandler.start();
			}
		});

		msg.handle(LoginKeyCallback.class, new ActionT<LoginKeyCallback>() {
			@Override
			public void call(LoginKeyCallback callback) {
				while (true) {
					if (authenticate(callback)) {
						Util.printConsole("Authenticated.", Bot.this, ConsoleColor.Magenta);
						break;
					} else {
						Util.printConsole("Retrying auth...", Bot.this, ConsoleColor.Red);
						try {
							Thread.sleep(2000);
						} catch (final InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				steamFriends.setPersonaName("[ScrapBank] " + info.getDisplayName());
				steamFriends.setPersonaState(EPersonaState.LookingToTrade);
				
				if (info.getId() == 1) {
					steamFriends.joinChat(steamFriends.getClanByIndex(0));
				}

				for (final SteamID bot : Main.bots) {
					if (steamFriends.getFriendRelationship(bot) != EFriendRelationship.Friend) {
						steamFriends.addFriend(bot);
					}
				}
				for (final long bot : BotInfo.getAdmins()) {
					final SteamID admin = new SteamID(bot);
					if (steamFriends.getFriendRelationship(admin) != EFriendRelationship.Friend) {
						steamFriends.addFriend(admin);
					}
				}
				Main.bots.add(steamClient.getSteamId());

				isLoggedIn = true;
				queueHandler.canTrade = true;
			}
		});

		msg.handle(PersonaStateCallback.class, new ActionT<PersonaStateCallback>() {
			@Override
			public void call(PersonaStateCallback callback) {
				if (callback.getFriendID() == steamUser.getSteamId()) {
					return;
				}

				final EFriendRelationship relationship = steamFriends.getFriendRelationship(callback.getFriendID());
				if (relationship == EFriendRelationship.Friend) {
					queueHandler.acceptedRequest(callback.getFriendID());
				} else if (relationship == EFriendRelationship.PendingInvitee) {
					Util.printConsole("Friend Request Pending: " + callback.getFriendID() + "(" + steamFriends.getFriendPersonaName(callback.getFriendID()) + ")", Bot.this, ConsoleColor.DarkCyan, true);
					if (Main.bots.contains(callback.getFriendID()) || BotInfo.getAdmins().contains(callback.getFriendID())) {
						steamFriends.addFriend(callback.getFriendID());
					}
				}
			}
		});

		msg.handle(ChatMsgCallback.class, new ActionT<ChatMsgCallback>() {
			@Override
			public void call(ChatMsgCallback callback) {
				//Type (emote or chat)
				final EChatEntryType type = callback.getChatMsgType();

				if (type == EChatEntryType.ChatMsg) {
					String response = "";
					if (responses.containsKey(callback.getMessage().toLowerCase())) {
						response = responses.get(callback.getMessage().toLowerCase());
					} else {
						final String[] args = callback.getMessage().split(" ");
						final String text = Util.removeArg0(callback.getMessage());
						final String[] pArgs = text.split(" ");

						Command cmd = Commands.getCommand(args[0]);
						if (cmd != Commands.unknown) {
							response = cmd.call(Bot.this, callback.getChatterID(), pArgs, text);
						}
					}
					steamFriends.sendChatRoomMessage(callback.getChatRoomID(), EChatEntryType.ChatMsg, response);
				}
			}
		});
		
		msg.handle(FriendMsgCallback.class, new ActionT<FriendMsgCallback>() {
			@Override
			public void call(FriendMsgCallback callback) {
				//Type (emote or chat)
				final EChatEntryType type = callback.getEntryType();

				if (type == EChatEntryType.ChatMsg) {
					String response = "";
					if (responses.containsKey(callback.getMessage().toLowerCase())) {
						response = responses.get(callback.getMessage().toLowerCase());
					} else {
						final String[] args = callback.getMessage().split(" ");
						final String text = Util.removeArg0(callback.getMessage());
						final String[] pArgs = text.split(" ");

						response = Commands.getCommand(args[0]).call(Bot.this, callback.getSender(), pArgs, text);
					}
					steamFriends.sendChatMessage(callback.getSender(), EChatEntryType.ChatMsg, response);
				}
			}
		});

		msg.handle(SessionStartCallback.class, new ActionT<SessionStartCallback>() {
			@Override
			public void call(SessionStartCallback call) {
				TradeListener listener = new ScrapTrade(Bot.this);
				if (BotInfo.getAdmins().contains(call.getOtherClient().convertToLong())) {
					listener = new AdminTrade(Bot.this);
				} else if (Main.bots.contains(call.getOtherClient())) {
					listener = new ExchangeTrade(Bot.this);
				}
				try {
					currentTrade = new Trade(steamUser.getSteamId(), call.getOtherClient(), sessionId, token, listener);
				} catch (final Exception e) {
					steamFriends.sendChatMessage(call.getOtherClient(), EChatEntryType.ChatMsg, "Could not retrieve a backpack, is your backpack private?\nPlease requeue to try again");
					steamTrade.cancelTrade(call.getOtherClient());
					queueHandler.tradeEnded();
					currentTrade = null;
				}
			}
		});

		msg.handle(TradeProposedCallback.class, new ActionT<TradeProposedCallback>() {
			@Override
			public void call(TradeProposedCallback thing) {
				Util.printConsole("Trade Proposed Callback. Other: " + thing.getOtherClient(), Bot.this, ConsoleColor.White, true);
				if (Main.bots.contains(thing.getOtherClient()) && queueHandler.needItemsBool) {
					steamTrade.respondToTrade(thing.getTradeID(), true);
				} else {
					//SteamTrade.RespondToTrade(thing.TradeID, false);
					steamFriends.sendChatMessage(thing.getOtherClient(), EChatEntryType.ChatMsg, "Please wait your turn, thanks :)");
				}
			}
		});

		msg.handle(TradeResultCallback.class, new ActionT<TradeResultCallback>() {
			@Override
			public void call(TradeResultCallback thing) {
				Util.printConsole("Trade Status: " + thing.getResponse(), Bot.this, ConsoleColor.Magenta, true);

				if (thing.getResponse() == EEconTradeResponse.Accepted) {
					if (!Main.bots.contains(thing.getOtherClient())) {
						Util.printConsole("Trade accepted!", Bot.this, ConsoleColor.Yellow);
					}
				} else if (thing.getResponse() == EEconTradeResponse.TargetAlreadyTrading) {
					Util.printConsole("User is already trading!", Bot.this, ConsoleColor.Magenta);
					steamFriends.sendChatMessage(thing.getOtherClient(), EChatEntryType.ChatMsg, "You're at the top of the trade queue, but are in trade. We don't have all day :c");
					try {
						Thread.sleep(10000);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					queueHandler.ignoredTrade(thing.getOtherClient());
				} else if (thing.getResponse() == EEconTradeResponse.Declined) {
					Util.printConsole("User declined trade???", Bot.this, ConsoleColor.Magenta);
					try {
						Thread.sleep(5000);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					queueHandler.ignoredTrade(thing.getOtherClient());
				} else if (thing.getResponse() == EEconTradeResponse.Cancel) {
					// Do nothing
				} else {
					Util.printConsole("Assume User Ignored Trade Request...", Bot.this, ConsoleColor.Magenta);
					queueHandler.ignoredTrade(thing.getOtherClient());
				}
			}
		});

		msg.handle(LoggedOffCallback.class, new ActionT<LoggedOffCallback>() {
			@Override
			public void call(LoggedOffCallback callback) {
				Util.printConsole("Told to log off by server (" + callback.getResult() + "), attemping to reconnect", Bot.this, ConsoleColor.Magenta);
				steamClient.connect();
			}
		});

		msg.handle(DisconnectedCallback.class, new ActionT<DisconnectedCallback>() {
			@Override
			public void call(DisconnectedCallback obj) {
				isLoggedIn = false;
				if (currentTrade != null) {
					currentTrade = null;
				}
				Util.printConsole("Disconnected from Steam Network, attemping to reconnect", Bot.this, ConsoleColor.Magenta);
				steamClient.connect();
			}
		});
	}

	/**
	 * Authenticate. This does the same as SteamWeb.DoLogin(),
	 * but without contacting the Steam Website.
	 * Should this one stop working, use SteamWeb.DoLogin().
	 * @param callback
	 * @return
	 */
	boolean authenticate(LoginKeyCallback callback) {
		sessionId = new BASE64Encoder().encode(String.valueOf(callback.getUniqueId()).getBytes());

		Util.printConsole("Got login key, performing web auth...", this, ConsoleColor.Magenta, true);

		final WebAPI userAuth = new WebAPI("ISteamUserAuth", BotInfo.getApiKey());
		// generate an AES session key
		final byte[] sessionKey = CryptoHelper.GenerateRandomBlock(32);

		// rsa encrypt it with the public key for the universe we're on
		byte[] cryptedSessionKey = null;
		final RSACrypto rsa = new RSACrypto(KeyDictionary.getPublicKey(steamClient.getConnectedUniverse()));
		cryptedSessionKey = rsa.encrypt(sessionKey);

		final byte[] loginKey = new byte[20];
		System.arraycopy(callback.getLoginKey().getBytes(), 0, loginKey, 0, callback.getLoginKey().length());

		// aes encrypt the loginkey with our session key
		final byte[] cryptedLoginKey = CryptoHelper.SymmetricEncrypt(loginKey, sessionKey);

		KeyValue authResult;

		try {
			authResult = userAuth.authenticateUser(String.valueOf(steamClient.getSteamId().convertToLong()), WebHelpers.UrlEncode(cryptedSessionKey), WebHelpers.UrlEncode(cryptedLoginKey), "POST");
		} catch (final Exception e) {
			return false;
		}

		token = authResult.get("token").asString();

		return true;
	}
}
