package com.botifier.imgdiff;

import static com.botifier.imgdiff.ImageHandler.*;

import java.io.IOException;
import java.util.Scanner;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

public class Main {

	public static void main(String[] args) throws IOException {
		if (args.length < 3) {
			System.out.println("Not enough arguments.");
			return;
		}
		//Delay for debugging, otherwise it will finish faster than a profiler can catch it
		//Scanner s = new Scanner(System.in);
		//System.out.print("Press Enter to Start.");
		//s.nextLine();
		
		//Loads the files
		byte[] i1 = loadFile(args[0]);
		byte[] i2 = loadFile(args[1]);
		
		//Creates a DiffContainer using the loaded images
		DiffContainer i3 = compare(createImageFromBytes(i1), createImageFromBytes(i2));
		//Creates an image representing the differences between the i3 and the original image i1
		BufferedImage dif = i3.diff(createImageFromBytes(i1));
		
		//Writes the diff image to a png file
		ImageIO.write(dif, "png", new File("diff_"+args[2]));
		
		//Writes the DiffContainer to a file
		try (FileOutputStream fos = new FileOutputStream(new File(args[2]+".diff"))) {
			byte[] bytes = i3.toBytes();
			fos.write(bytes);
		}
		
		//Loads the diff file again for testing
		//TODO: Move this to a test class
		try (FileInputStream fis = new FileInputStream(new File(args[2]+".diff"))) {
			//Create an ByteArrayOutputStream
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			//Iterate through the file stream and write it to the ByteBuffer
			while (fis.available() > 0) {
				byte read = (byte) fis.read();
				buffer.write(read);
			}
			
			//Converts the buffer into a byte array
			byte[] buff = buffer.toByteArray();
			
			//Converts the byte array to a DiffContainer
			DiffContainer dc = DiffContainer.fromBytes(buff);
			
			//Ensure that the file was loaded properly
			assert dc.equals(i3) : "Loaded File is somehow not the same as the original.";
			//Ensure that it isn't an arbitrary blank DiffContainer
			assert !dc.equals(new DiffContainer(1, 1)) : "Loaded file is somehow equal to a generic empty version.";
		}
	}
	
}
