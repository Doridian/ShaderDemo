package de.doridian.crtdemo;

import de.doridian.crtdemo.basic.BaseCompiledProgram;
import de.doridian.crtdemo.basic.BasicFunctions;
import de.doridian.crtdemo.basic.BasicIO;
import de.doridian.crtdemo.basic.CodeParser;
import de.doridian.crtdemo.shader.MainShader;
import de.doridian.crtdemo.shader.OpenGLMain;
import de.doridian.crtdemo.shader.ShaderProgram;
import de.doridian.crtdemo.simfs.FileSystem;
import de.doridian.crtdemo.simfs.interfaces.IFileData;
import de.doridian.crtdemo.simfs.interfaces.IFileSystem;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CRTDemoMain extends OpenGLMain {
	static int promptInputAllowed = 0;
	static boolean cursorBlinkVisible = false;
	static String promptInput = "";
	static LinkedBlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

	//32 rows, 16 lines
	//670x510 screen area

	static char[][] screenCursorOff = new char[16][32];
	static char[][] screenCursorOn = new char[16][32];
	static int posX = 0;
	static int posY = 0;

	static String stringCursorOff = "";
	static String stringCursorOn = "";

	public static void blankLine(int line) {
		char[] lineData = new char[32];
		char[] lineData2 = new char[32];
		for(int i = 0; i < 32; i++) {
			lineData[i] = ' ';
			lineData2[i] = ' ';
		}
		screenCursorOff[line] = lineData;
		screenCursorOn[line] = lineData2;
		refreshCursor();
	}

	public static void refreshScreen() {
		StringBuilder sbCursorOn = new StringBuilder();
		StringBuilder sbCursorOff = new StringBuilder();
		for(int i = 0; i < 16; i++) {
			sbCursorOn.append(BasicFunctions.RTRIM$(new String(screenCursorOn[i])));
			sbCursorOn.append('\n');
			sbCursorOff.append(BasicFunctions.RTRIM$(new String(screenCursorOff[i])));
			sbCursorOff.append('\n');
		}
		stringCursorOn = BasicFunctions.RTRIM$(sbCursorOn.toString());
		stringCursorOff = BasicFunctions.RTRIM$(sbCursorOff.toString());
	}

	public static void writeChar(char c) {
		scrollUp();
		screenCursorOff[posY][posX] = c;
		screenCursorOn[posY][posX] = c;
		refreshCursor();
	}

	public static void scrollUp() {
		if(posY >= 16)
			doScrollUp();
	}

	public static void nextLine() {
		posX = 31;
		moveForward();
	}

	public static void moveForward() {
		++posX;
		while(posX >= 32) {
			posX -= 32;
			++posY;
		}
		refreshCursor();
		scrollUp();
	}

	private static int cursorX = 0, cursorY = 0;

	public static synchronized void refreshCursor() {
		if(cursorY < 16)
			screenCursorOn[cursorY][cursorX] = screenCursorOff[cursorY][cursorX];
		if(posY < 16)
			screenCursorOn[posY][posX] = '\u00DC';
		cursorX = posX;
		cursorY = posY;
		refreshScreen();
	}

	public static void doScrollUp() {
		posX = 0;
		int mov = posY - 15;
		posY = 15;
		for(int i = mov; i < 16; i++) {
			screenCursorOff[i - mov] = screenCursorOff[i];
			System.arraycopy(screenCursorOff[i - mov], 0, screenCursorOn[i - mov], 0, 32);
		}
		for(int i = 16 - mov; i < 16; i++)
			blankLine(i);
		refreshCursor();
	}

	public static class CRTBasicIO implements BasicIO {
		@Override
		public String readLine() {
			promptInputAllowed++;
			try {
				return inputQueue.take();
			} catch (InterruptedException e) {
				return "";
			}
		}

		@Override
		public synchronized void print(Object obj) {
			for(char c : obj.toString().toCharArray()) {
				if(c == '\r') {
					posX = 0;
					continue;
				}
				if(c == '\n') {
					nextLine();
					continue;
				}
				writeChar(c);
				moveForward();
			}
		}

		@Override
		public void setCursor(int x, int y) {
			posX = x % 16;
			posY = y % 16;
			refreshCursor();
		}

		@Override
		public void clearLine(int line) {
			blankLine(line);
		}

		@Override
		public void clearScreen() {
			blankScreen();
		}

		@Override
		public int getLines() {
			return 16;
		}

		@Override
		public int getColumns() {
			return 32;
		}
	}

	private static MainShader makeShader(String resource) {
		return new MainShader(ShaderProgram.VSH_DONOTHING, null, Util.readStreamFully(CRTDemoMain.class.getResourceAsStream("/" + resource + ".fsh")));
	}

	private static void blankScreen() {
		for(int i = 0; i < 16; i++)
			blankLine(i);
		refreshCursor();
	}

	public static void main(String[] args) throws Exception {
		blankScreen();

		final int THREAD_SLEEP_DIVIDER = 1;

		Thread basicThread = new Thread() {
			private final BasicIO io = new CRTBasicIO();

			private void doSleep(int millis) {
				try { Thread.sleep(millis / THREAD_SLEEP_DIVIDER); } catch (InterruptedException e) { }
			}

			private void halt() {
				io.print("\n=== CPU HALT ===\n");
			}

			public void run() {
				//
				try {
					RandomAccessFile ranAF = new RandomAccessFile("data/filesystem/C", "rw");
					ranAF.setLength(0);
					IFileSystem fs = FileSystem.create(512, 32000, ranAF);
					IFileData file = fs.getRootDirectory().createFile("boot.basic");
					file.write(Util.readFile("data/test.basic").getBytes("ASCII"));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
				//

				io.print("foxBIOS v0.1b\nHit return to boot ");
				io.readLine();
				io.print("Booting...\n");

				doSleep(500);
				io.print("Initializing disks...\n");
				doSleep(2000);

				final DriveGroup driveGroup;
				try {
					driveGroup = new DriveGroup(new File("data/filesystem"));
					if(!driveGroup.drives.containsKey('A'))
						driveGroup.drives.put('A', null);
				} catch (IOException e) {
					e.printStackTrace();
					io.print("ERROR\n");
					System.exit(0);
					return;
				}

				final TreeMap<Character, IFileSystem> drives = driveGroup.getDrives();

				for(Map.Entry<Character, IFileSystem> drive : drives.entrySet()) {
					io.print("" + drive.getKey() + ":" + FileSystem.PATH_SEPARATOR);

					if(drive.getValue() != null)
						io.print(" " + drive.getValue().getClusterCount() + "C, " + drive.getValue().getClusterSize() + "BPC\n");
					else
						io.print(" EMPTY\n");

					doSleep(2000);
				}

				io.print("All disks initialized.\nFinding boot.basic...\n");

				for(Character driveLetter : drives.navigableKeySet()) {
					String bootFileName = driveLetter + ":" + FileSystem.PATH_SEPARATOR + "boot.basic";
					io.print("Trying " + bootFileName + " ");
					doSleep(1000);

					if(drives.get(driveLetter) == null) {
						io.print("NO DISK\n");
						continue;
					}

					IFileData bootFile;

					try {
						bootFile = (IFileData)driveGroup.getFile(bootFileName);
						if (bootFile == null)
							throw new FileNotFoundException();
					} catch (Exception e) {
						e.printStackTrace();
						io.print("NOT FOUND\n");
						continue;
					}

					BaseCompiledProgram program;

					try {
						CodeParser parser = new CodeParser(bootFile, true);
						program = parser.compile();
					} catch (Exception e) {
						io.print("ERROR\n");
						continue;
					}

					io.print("OK\nInvoking...");
					doSleep(1000);

					driveGroup.currentDrive = driveLetter;

					program.$start(io);
					halt();
					return;
				}

				io.print("\n=== NO BOOTABLE MEDIA ===\n");
				halt();
			}
		};
		basicThread.setDaemon(true);
		basicThread.start();

		OpenGLMain.main(new Renderer() {
			private AngelCodeFont font;
			private MainShader[] shaders;

			private int frameCounter = 0;

			@Override
			public void initialize() {
				try {
					font = new AngelCodeFont("greenscreen", CRTDemoMain.class.getResourceAsStream("/greenscreen.fnt"), CRTDemoMain.class.getResourceAsStream("/greenscreen_0.png"), true);
				} catch (Exception e) {
					e.printStackTrace();
				}

				shaders = new MainShader[] {
					makeShader("scanlines"),
					makeShader("gblur_h"),
					makeShader("gblur_v")
				};
			}

			@Override
			public void keyPressed(int keyCode, char keyChar) {
				if(promptInputAllowed == 0)
					return;

				if(keyCode == Keyboard.KEY_BACK) {
					if(promptInput.length() > 0) {
						promptInput = promptInput.substring(0, promptInput.length() - 1);
						if(--posX < 0) {
							if(--posY < 0) {
								posY = 0;
								return;
							}
						}
						refreshCursor();
						writeChar(' ');
					}
					return;
				}

				if(keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
					inputQueue.add(promptInput);
					promptInput = "";
					nextLine();
					promptInputAllowed--;
					return;
				}

				if(keyChar != 0) {
					if(promptInput.length() >= 32)
						return;
					writeChar(keyChar);
					moveForward();
					promptInput += keyChar;
				}
			}

			@Override
			public void renderFrame() {
				org.newdawn.slick.opengl.TextureImpl.unbind();

				if(frameCounter++ > 60) {
					cursorBlinkVisible = !cursorBlinkVisible;
					frameCounter = 0;
				}

				String actualDrawData;
				if(cursorBlinkVisible && promptInputAllowed != 0)
					actualDrawData = stringCursorOn;
				else
					actualDrawData = stringCursorOff;

				font.drawString(0, 0, actualDrawData, Color.green);

				for(int i = 0; i < shaders.length; i++) {
					OpenGLMain.flipBuffers(shaders[i]);
				}
			}
		});
	}
}
