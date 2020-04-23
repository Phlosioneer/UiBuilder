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
}
