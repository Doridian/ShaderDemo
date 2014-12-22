package de.doridian.crtdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

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
			addLibDir("./" + NATIVES_DIR.getName() + "/");
			System.out.println("Natives loaded!");
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	private static void addLibDir(String s) throws IOException {
		try {
			// This enables the java.library.path to be modified at runtime
			// From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
			//
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[])field.get(null);
			for (int i = 0; i < paths.length; i++) {
				if (s.equals(paths[i])) {
					return;
				}
			}
			String[] tmp = new String[paths.length+1];
			System.arraycopy(paths,0,tmp,0,paths.length);
			tmp[paths.length] = s;
			field.set(null,tmp);
			System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + s);
		} catch (IllegalAccessException e) {
			throw new IOException("Failed to get permissions to set library path");
		} catch (NoSuchFieldException e) {
			throw new IOException("Failed to get field handle to set library path");
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
