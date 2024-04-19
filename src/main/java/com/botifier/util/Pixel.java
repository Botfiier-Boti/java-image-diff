package com.botifier.util;

import java.util.Objects;

public final class Pixel {

	//X position of the pixel
	public final int x;
	//Y position of the pixel
	public final int y;
	//Alpha value of the pixel
	public final short alpha;
	
	/**
	 * Pixel Constructor
	 * Immutable
	 * @param x X position of the pixel
	 * @param y Y position of the pixel
	 * @param alpha Alpha value of the pixel
	 */
	public Pixel(int x, int y, short alpha) {
		this.x = x;
		this.y = y;
		this.alpha = (short) (alpha & 0xFF);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y, alpha);
	}
	
	@Override 
	public boolean equals(Object o) {
		if (!(o instanceof Pixel))
			return false;
		Pixel comp = (Pixel) o;
		return comp.x == x && comp.y == y && comp.alpha == alpha;
	}
}
