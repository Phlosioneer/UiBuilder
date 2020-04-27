package main;

import java.util.HashMap;

public class Rectangle {

	public String name;
	public double x;
	public double y;
	public double width;
	public double height;

	/**
	 * Can be null.
	 */
	public HashMap<String, Object> properties;

	/**
	 * Used by Gson
	 */
	public Rectangle() {
		this(0, 0, 0, 0);
	}

	public Rectangle(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		name = "";
		properties = null;
	}

	@Override
	public String toString() {
		if (name.isBlank()) {
			var ret = "(x:" + formatDecimal(x, 2);
			ret += ", y:" + formatDecimal(y, 2);
			ret += ", width:" + formatDecimal(width, 2);
			ret += ", height:" + formatDecimal(height, 2);
			ret += ")";
			return ret;
		} else {
			return name;
		}
	}

	// TODO: This might become slow for large lists of rectangles. It can be made faster.
	private static String formatDecimal(double value, int places) {
		int rounded = (int) Math.round(value * Math.pow(10, places));
		String ret = Integer.toString(rounded);
		while (ret.length() < places) {
			ret = '0' + ret;
		}
		return "0." + ret;
	}
}
