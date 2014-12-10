package de.doridian.crtdemo;

import java.io.*;

public class Util {
	public static String readStreamFully(InputStream inputStream) {
		java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static String readFile(String _file) {
		try {
			File file = new File(_file);
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();

			return new String(data, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeFile(String _file, String content) {
		try {
			File file = new File(_file);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(content.getBytes("UTF-8"));
			fos.close();
		} catch (IOException e) {
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
