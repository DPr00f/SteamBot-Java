package uk.co.thomasc.scrapbanktf.util;

public class DualMInt {
	private final MutableInt int1;
	private final MutableInt int2;

	public DualMInt(int _1, int _2) {
		int1 = new MutableInt(_1);
		int2 = new MutableInt(_2);
	}

	public MutableInt get1() {
		return int1;
	}

	public MutableInt get2() {
		return int2;
	}

	public int diff() {
		return get1().get() - get2().get();
	}
}
