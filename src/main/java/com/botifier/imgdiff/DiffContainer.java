package com.botifier.imgdiff;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.botifier.util.Pixel;
import com.botifier.util.PixelColor;

public class DiffContainer {
	//Dimensions of the container
	private int width = 0;
	private int height = 0;
	//Pixel Map
	private HashMap<PixelColor, ArrayList<Pixel>> pixels = new HashMap<PixelColor, ArrayList<Pixel>>();
	
	/**
	 * DiffContainer Constructor
	 * @param width Width of the image this container represents
	 * @param height Height of the image this container represents
	 */
	public DiffContainer(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Puts a pixel on the pixel map
	 * @param key PixelColor the color of the pixel
	 * @param p Pixel contains other information on the pixel such as alpha along with x and y positions.
	 * @return boolean Whether or not the pixel was added to the map
	 */
	public boolean putPixel(PixelColor key, Pixel p) {
		ArrayList<Pixel> pix = pixels.putIfAbsent(key, new ArrayList<Pixel>());
		if (pix == null) {
			pix = pixels.get(key);
		}
		
		return pix.add(p);
	}
	
	/**
	 * Returns all pixels of the specified color
	 * @param key PixelColor color to search for
	 * @return
	 */
	public ArrayList<Pixel> getPixels(PixelColor key) {
		return pixels.getOrDefault(key, new ArrayList<Pixel>());
	}
	
	/**
	 * Returns this container as a BufferedImage
	 * @return The Equivalent as a BufferedImage
	 */
	public BufferedImage asImage() {
		//Generate a blank image
		BufferedImage cr = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		//Iterate through all pixel colors
		for (PixelColor p : pixels.keySet()) {
			
			//Iterate through all pixels related to this color
			for (Pixel pix : pixels.get(p)) {
				int x = pix.x;
				int y = pix.y;
				
				int r = Math.abs(p.r) & 0xFF;
				int g = Math.abs(p.g) & 0xFF;
				int b = Math.abs(p.b) & 0xFF;
				int a = Math.abs(pix.alpha) & 0xFF;
				
				//Sets the color in the BufferedImage at the specific location
				cr.setRGB(x, y, (a << 24 | r << 16 | g << 8 | b));
			}
		}
		
		return cr;
	}
	
	/**
	 * Calculates the difference between this and a BufferedImage
	 * Result will be the value changes required to produce the supplied image
	 * @param bi Image to compare
	 * @return BufferedImage
	 */
	public BufferedImage diff(BufferedImage bi) {
		//Maximum dimensions
		int maxWidth = Math.max(bi.getWidth(), width);
		int maxHeight = Math.max(bi.getHeight(), height);
		//Generate a blank image
		BufferedImage cr = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
		
		//Iterate through all pixel colors
		for (PixelColor p : pixels.keySet()) {
			
			//Iterate through all pixels related to the color
			for (Pixel pix  : pixels.get(p)) {
				int x = pix.x;
				int y = pix.y;
				
				//If x or y exceed the dimensions of the checked image set color to p
				if (x >= bi.getWidth() || y >= bi.getHeight()) {
					cr.setRGB(x, y, p.getRGB());
					continue;
				}
				//Converts the color in bi to a PixelColor
				PixelColor use = new PixelColor(bi.getRGB(x, y));
				
				//Adds colors together
				short r = (short) (use.r + p.r);
				short g =	(short) (use.g + p.g);
				short b = (short) (use.b + p.b);
				short a = (short) (use.a + pix.alpha);
				
				//Combines the values into a single integer
				int rgba = a << 24 | r << 16 | g << 8 | b;
				cr.setRGB(x, y, rgba);
			}
			
		}
		
		return cr;
	}
	
	/**
	 * Converts this container into a byte array
	 * @return This array as a byte array
	 */
	public byte[] toBytes() {
		//Track the size in bytes, used for testing
		int size = 0;
		System.out.println("Big:" +pixels.keySet().size());
		//Open a ByteArrayOutputStream
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			//Writes a header 
			oos.writeInt(width);
			oos.writeInt(height);
			oos.writeInt(pixels.size());
			size += 12;
			
			for (PixelColor p : pixels.keySet()) {
				ArrayList<Pixel> h = pixels.get(p);
				//Writes pixel color data
				oos.writeShort(p.r);
				oos.writeShort(p.g);
				oos.writeShort(p.b);
				oos.writeInt(h.size());
				size += 10;
				
				for (Pixel pix  : h) {
					//Writes pixel location data
					oos.writeInt(pix.x);
					oos.writeInt(pix.y);
					oos.writeShort(pix.alpha);
					size += 10;
				}
			}
			//This can be removed, but I like it.
			System.out.println((size/1000)+"kb");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bos.toByteArray();
	}

	/**
	 * Creates a DiffContainer from a byte array
	 * @param bytes byte array to use
	 * @return The resulting DiffContainer
	 */
	public static DiffContainer fromBytes(byte[] bytes) {
		//Create an empty variable
		DiffContainer hold = null;
		try (ObjectInputStream bai = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			//Print debugging
			System.out.println(bai.available());
			
			//Read Header
			int width = bai.readInt();
			int height = bai.readInt();
			int pixels = bai.readInt();
			
			//Create a DiffContainer using this information
			hold = new DiffContainer(width, height);
			
			//Iterate through each pixel color
			for (int i = 0; i < pixels; i++) {
				//Read pixel header
				short r = bai.readShort();
				short g = bai.readShort();
				short b = bai.readShort();
				int read = bai.readInt();
				
				//Iterate through pixel information
				for (int e = 0; e < read; e++ ) {
					int x = bai.readInt();
					int y = bai.readInt();
					short a = bai.readShort();	
					
					//Add the pixel to the pixel map
					hold.putPixel(new PixelColor(r,g,b,a), new Pixel(x, y, a));
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return hold;
	}
	
	@Override
	public boolean equals(Object o) {
		//If o is not a DiffContainer it isn't a DiffContainer
		if (!(o instanceof DiffContainer)) 
			return false;
		//Cast o as a DiffContainer
		DiffContainer two = (DiffContainer) o;
		
		//Check if the keyset matches
		boolean keys = two.pixels.keySet().containsAll(pixels.keySet());
		//Checks if the values match
		boolean values = two.pixels.values().containsAll(pixels.values());
		//Checks if the sizes match
		boolean sizes = (width == two.width) && (height == two.height);
		
		//Returns true if all are true
		return keys && values && sizes;
	}
}
