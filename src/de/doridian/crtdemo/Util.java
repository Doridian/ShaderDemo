package de.doridian.crtdemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Util {
	public static String readFile(String _file) {
		try {
			File file = new File(_file);
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();

			return new String(data, "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeFile(String _file, String content) {
		try {
			File file = new File(_file);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(content.getBytes("UTF-8"));
			fos.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void printByteArray(String p, byte[] arr) {
		System.out.print(p + ": ");
		for(byte b : arr)
			System.out.print(b + " ");
		System.out.println();
	}
}
