package de.doridian.crtdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class LWJGLExtract {
	private static final String[] LWJGL_LIBS = {
		"OpenAL32.dll",
		"OpenAL64.dll",
		"jinput-dx8.dll",
		"jinput-dx8_64.dll",
		"jinput-raw.dll",
		"jinput-raw_64.dll",
		"jinput-wintab.dll",
		"libjinput-linux.so",
		"libjinput-linux64.so",
		"liblwjgl.so",
		"liblwjgl64.so",
		"libopenal.so",
		"libopenal64.so",
		"lwjgl.dll",
		"lwjgl64.dll",
		"openal.dylib"
	};

	private static final File NATIVES_DIR = new File("natives");

	public static void extractLibs() {
		try {
			NATIVES_DIR.mkdirs();
			for(String str : LWJGL_LIBS)
				extractLibsInt(str);
			System.setProperty("java.library.path", "./" + NATIVES_DIR.getName() + "/");
			System.out.println("Natives loaded: " + System.getProperty("java.library.path", "N/A"));
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	private static void extractLibsInt(String lib) throws Throwable {
		File outLib = new File(NATIVES_DIR, lib);
		if(outLib.exists())
			return;
		InputStream stream = LWJGLExtract.class.getResourceAsStream("/" + lib);
		if(stream == null)
			return;
		FileOutputStream fos = new FileOutputStream(outLib);
		byte[] buffer = new byte[4096];
		int read = 0;
		while((read = stream.read(buffer)) > 0) {
			fos.write(buffer, 0, read);
		}
		fos.close();
		stream.close();
	}
}
