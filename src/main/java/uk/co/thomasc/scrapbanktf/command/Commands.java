package uk.co.thomasc.scrapbanktf.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public enum Commands {
	SAY(Say.class),
	TELL(Tell.class),
	CHECKINV(CheckInv.class),
	MISSING(Missing.class),
	QUOTE(Quote.class), ;

	public static Unknown unknown = new Unknown();
	private Command cmd;

	private Commands(Class<? extends Command> clazz) {
		try {
			final Constructor<? extends Command> c = clazz.getDeclaredConstructor(new Class[] {});
			cmd = c.newInstance();
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
		} catch (final SecurityException e) {
			e.printStackTrace();
		} catch (final InstantiationException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private static HashMap<String, Commands> map = new HashMap<String, Commands>();

	public static Command getCommand(String cmd) {
		if (Commands.map.containsKey(cmd)) {
			return Commands.map.get(cmd).cmd;
		} else {
			return Commands.unknown;
		}
	}

	static {
		for (final Commands ch : Commands.values()) {
			Commands.map.put(ch.toString().toLowerCase(), ch);
		}
	}

}
