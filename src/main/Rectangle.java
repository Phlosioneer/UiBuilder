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
			var builder = new StringBuilder();
			builder.append("(x:");
			builder.append(Double.toString(x), 0, 5);
			builder.append(", y:");
			builder.append(Double.toString(y), 0, 5);
			builder.append(", w:");
			builder.append(Double.toString(width), 0, 5);
			builder.append(", h:");
			builder.append(Double.toString(height), 0, 5);
			builder.append(')');
			return builder.toString();
		} else {
			return name;
		}
	}
}
