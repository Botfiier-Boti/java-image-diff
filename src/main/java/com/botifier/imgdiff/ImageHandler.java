package com.botifier.imgdiff;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import com.botifier.util.Pixel;
import com.botifier.util.PixelColor;

public class ImageHandler {
	public static final int PNG_HEADER_LENGTH = 25;
	
	/***
	 * Loads a file as bytes
	 * 
	 * Note: Uses a single byte array, loading extremely large files using this function may crash the program
	 * depending on whether or not a single byte array can contain it.
	 * 
	 * @param location Location of the file relative to the execution location
	 * @return byte[] Array of the file's raw bytes
	 * @throws IOException
	 */
	public static byte[] loadFile(String location) throws IOException {
		// Creates a file object, alternatively could just use Path directly.
		File image = new File(location);
		
		//Uses Files.readAllBytes in order to load the file into a byte array
		//Not a good idea to use this for large files
		byte[] contents = Files.readAllBytes(image.toPath());
		
		return contents;
	}
	
	/***
	 * Uses an ByteArrayOutputStream and ImageIO in order to convert The data within a BufferedImage
	 * into raw bytes.
	 * 
	 * @param bi The BuffferedImage to convert
	 * @param format the format to use e.g. jpeg, png, bmp. Same as ImageIO.write
	 * @return
	 */
	public static byte[] bufferedImageToByteArray(BufferedImage bi, String format) {
		try {
			//Creates an ByteArrayOutputStream
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			
			//Writes the data within the BufferedImage in the specified format into the OutputStream
			ImageIO.write(bi, format, bao);
			
			//Pulls the byte array from the OutputStream
			byte[] bytes = bao.toByteArray();
			
			//Returns the array
			return bytes;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/***
	 * Compares two BufferedImages and produces a DiffContainer
	 * 
	 * @param i1 Primary Image
	 * @param i2 Secondary Image
	 * @return DiffContainer
	 */
	public static DiffContainer compare(BufferedImage i1, BufferedImage i2) {
		int maxWidth = Math.max(i1.getWidth(), i2.getWidth());
		int maxHeight = Math.max(i1.getHeight(), i2.getHeight());
		
		DiffContainer dc = new DiffContainer(maxWidth, maxHeight);
		
		for (int y = 0; y < maxHeight; y++) {
			for (int x = 0; x < maxWidth; x++) {
				//If the bounds are outside the first image continue
				if (x >= i1.getWidth() || y >= i1.getHeight()) {
					continue;
				}
				//if the bounds are outside the second image continue
				if (x >= i2.getWidth() || y >= i2.getHeight()) {
					continue;
				}
				
				//Get the colors at the current location
				int rgb1 = i1.getRGB(x, y);
				int rgb2 = i2.getRGB(x, y);
				
				//Converts those colors into a pixel object
				PixelColor p1 = new PixelColor(rgb1);
				PixelColor p2 = new PixelColor(rgb2);
				
				//Gets the difference between the two pixels
				PixelColor dif = p1.diff(p2); 
				
				//Adds the Pixel to the DiffContainer
				dc.putPixel(dif, new Pixel(x, y, dif.a));
			}
		}
		
		return dc;
	}
	
	/***
	 * Converts a byte array into a BufferedImage using ImageIO
	 * @param b byte array to convert
	 * @return The resulting BufferedImage
	 * @throws IOException
	 */
	public static BufferedImage createImageFromBytes(byte[] b) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(b);
		return ImageIO.read(is);
	}
	
	/**
	 * Creates a deep copy of the specified BufferedImage
	 * @param bi BufferedImage to copy
	 * @return BufferedImage A copy of the supplied image
	 */
	public static BufferedImage deepCopy(BufferedImage bi) {
		WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
		
		return new BufferedImage(bi.getColorModel(), raster, bi.isAlphaPremultiplied(), null);
	}
}
