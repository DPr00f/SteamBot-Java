package uk.co.thomasc.scrapbanktf.util;

public enum ConsoleColor {
	// Summary:
	//     The color black.
	Black("\u001B[30m", 0),
	//
	// Summary:
	//     The color dark blue.
	DarkBlue("\u001B[34m", 1),
	//
	// Summary:
	//     The color dark green.
	DarkGreen("\u001B[32m", 2),
	//
	// Summary:
	//     The color dark cyan (dark blue-green).
	DarkCyan("\u001B[36m", 3),
	//
	// Summary:
	//     The color dark red.
	DarkRed("\u001B[31m", 4),
	//
	// Summary:
	//     The color dark magenta (dark purplish-red).
	DarkMagenta("\u001B[35m", 5),
	//
	// Summary:
	//     The color dark yellow (ochre).
	DarkYellow("\u001B[33m", 6),
	//
	// Summary:
	//     The color gray.
	Gray("\u001B[37m", 7),
	//
	// Summary:
	//     The color dark gray.
	DarkGray("\u001B[30m", 8),
	//
	// Summary:
	//     The color blue.
	Blue("\u001B[34m", 9),
	//
	// Summary:
	//     The color green.
	Green("\u001B[32m", 10),
	//
	// Summary:
	//     The color cyan (blue-green).
	Cyan("\u001B[36m", 11),
	//
	// Summary:
	//     The color red.
	Red("\u001B[31m", 12),
	//
	// Summary:
	//     The color magenta (purplish-red).
	Magenta("\u001B[35m", 13),
	//
	// Summary:
	//     The color yellow.
	Yellow("\u001B[33m", 14),
	//
	// Summary:
	//     The color white.
	White("\u001B[37m", 15),
	//
	// Summary:
	//     Reset escape
	Reset("\u001B[0m", 15), ;

	private String code;
	private int i;

	private ConsoleColor(String code, int i) {
		this.code = code;
		this.i = i;
	}

	public String v() {
		return code;
	}

	public int getInt() {
		return i;
	}
}
