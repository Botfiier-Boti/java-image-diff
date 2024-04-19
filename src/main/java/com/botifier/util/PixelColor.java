package com.botifier.util;

import java.util.Objects;

public final class PixelColor {
	//Color information
	public final short r;
	public final short g;
	public final short b;
	public final short a;
	
	
	/***
	 * PixelColor constructor
	 * Immutable
	 * @param x X location of the pixel
	 * @param y Y location of the pixel
	 * @param rgb The RGB information of the pixel in the format of ARGB. Looks like this in hex FF FF FF FF
	 */
	public PixelColor(int rgb) {
		this.r = (short) ((rgb >> 16) & 0xFF);
		this.g = (short) ((rgb >> 8) & 0xFF);
		this.b = (short) (rgb & 0xFF);
		this.a = (short) ((rgb >> 24) & 0xFF);
	}
	
	/***
	 * PixelColor constructor for pixels with negative values
	 * Used for containing differences
	 * Immutable
	 * @param x X location of the pixel
	 * @param y Y location of the pixel
	 * @param r Red value of the pixel 
	 * @param g Green value of the pixel
	 * @param b Blue value of the pixel
	 * @param a Alpha value of the pixel
	 */
	public PixelColor(short r, short g, short b, short a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	/**
	 * Returns the RGB value of this PixelColor in a 
	 * @return int
	 */
	public int getRGB() {
		return a << 24 | r << 16| g << 8 | b;
	}
	
	public PixelColor diff(PixelColor p) {
		short r = (short) (p.r - this.r) ;
		short g = (short) (p.g - this.g);
		short b = (short) (p.b - this.b);
		short a = (short) (p.a - this.a);
		
		return new PixelColor(r, g, b, a);
	}
	
	@Override 
	public String toString() {
		return a + " " + r + " " + g + " " + b;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(r, g, b);
	}
	
	@Override 
	public boolean equals(Object o) {
		//If o is not a PixelColor they are not equal
		if (!(o instanceof PixelColor))
			return false;
		//Casts the Object into a PixelColor
		PixelColor comp = (PixelColor) o;
		//Checks if the r g b values are the same
		return comp.r == r  && comp.g == g && comp.b == b;
	}
	
}
