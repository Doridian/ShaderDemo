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
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

public class CRTDemoMain extends OpenGLMain {
	static int promptInputAllowed = 0;

	static Integer promptCharInput = -1;

	static boolean cursorBlinkVisible = false;
	static String promptInput = "";
	static LinkedBlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

	//32 rows, 16 lines
	//670x510 screen area

	static char[][] screenCursorOff = new char[16][32];
	static char[][] screenCursorOn = new char[16][32];
	static int posX = 0;
	static int posY = 0;

	static boolean[][] screenInvert = new boolean[16][32];

	private static final Object invertLock = new Object();
	static int[] screenInvertX = new int[0];
	static int[] screenInvertY = new int[0];

	static String stringCursorOff = "";
	static String stringCursorOn = "";

	public static void blankLine(int line) {
		char[] lineData = new char[32];
		char[] lineData2 = new char[32];
		screenInvert[line] = new boolean[32];
		for(int i = 0; i < 32; i++) {
			lineData[i] = ' ';
			lineData2[i] = ' ';
		}
		screenCursorOff[line] = lineData;
		screenCursorOn[line] = lineData2;
		refreshInvert();
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

	public static void writeChar(char c, boolean invert) {
		scrollUp();
		screenInvert[posY][posX] = invert;
		screenCursorOff[posY][posX] = c;
		screenCursorOn[posY][posX] = c;
		refreshInvert();
		refreshCursor();
	}

	public static void writeChar(char c) {
		writeChar((char)(c & 0x7F), (c | 0x80) == 0x80);
	}

	public static void scrollUp() {
		if(posY >= 16)
			doScrollUp();
	}

	public static void nextLine() {
		posX = 31;
		moveForward();
	}

	private static class Point2D {
		public final int x;
		public final int y;

		public Point2D(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	public static synchronized void refreshInvert() {
		ArrayList<Point2D> invertP = new ArrayList<>();

		for(int y = 0; y < 16; y++) {
			boolean[] curInvertRow = screenInvert[y];
			for(int x = 0; x < 32; x++) {
				if(curInvertRow[x])
					invertP.add(new Point2D(x, y));
			}
		}

		int[] invX = new int[invertP.size()];
		int[] invY = new int[invertP.size()];
		for(int i = 0; i < invX.length; i++) {
			invX[i] = invertP.get(i).x;
			invY[i] = invertP.get(i).y;
		}

		synchronized (invertLock) {
			screenInvertX = invX;
			screenInvertY = invY;
		}
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
			screenInvert[i - mov] = screenInvert[i];
		}
		for(int i = 16 - mov; i < 16; i++)
			blankLine(i);
		refreshInvert();
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
		public synchronized int readChar() {
			promptCharInput = null;
			try {
				while(promptCharInput == null)
					Thread.sleep(10);
				int ret = promptCharInput;
				promptCharInput = -1;
				return ret;
			} catch (InterruptedException e) {
				return -1;
			}
		}

		@Override
		public void print(Object obj) {
			print(obj, false);
		}

		@Override
		public synchronized void print(Object obj, boolean invert) {
			for(char c : obj.toString().toCharArray()) {
				if(c == '\r') {
					posX = 0;
					continue;
				}
				if(c == '\n') {
					nextLine();
					continue;
				}
				writeChar(c, invert);
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

			private void printLoad(char c, int count, int totalTime, String endStr) {
				for(int i = 0; i < count; i++) {
					doSleep(totalTime / count);
					io.print(c);
				}
				io.print(endStr);
			}

			private void halt() {
				io.print("\n=== CPU HALT ===\n");
			}

			public void run() {
				FSTransferAgent.initFS();
				FSTransferAgent.transferFile("boot.basic");
				FSTransferAgent.transferFile("test_1.basic");
				FSTransferAgent.transferFile("test_2.basic");
				FSTransferAgent.transferFile("test_3.basic");
				FSTransferAgent.transferFile("test_4.basic");
				FSTransferAgent.transferFile("test_5.basic");

				io.print("foxBIOS v0.1b\nCore booting");
				printLoad('.', 3, 3000, " OK\n");

				io.print("Initializing disk bus");
				printLoad('.', 3, 2000, " ");

				final DriveGroup driveGroup;
				try {
					driveGroup = new DriveGroup(new File("data/filesystem"));
					if(!driveGroup.drives.containsKey('A'))
						driveGroup.drives.put('A', null);
				} catch (IOException e) {
					e.printStackTrace();
					io.print("ERROR\n");
					halt();
					return;
				}

				io.print("OK\n");

				final TreeMap<Character, IFileSystem> drives = driveGroup.getDrives();

				for(Map.Entry<Character, IFileSystem> drive : drives.entrySet()) {
					io.print("" + drive.getKey() + ":" + FileSystem.PATH_SEPARATOR + " ");

					printLoad('.', 3, 500, " ");

					if(drive.getValue() != null)
						io.print(drive.getValue().getClusterCount() + "C, " + drive.getValue().getClusterSize() + "BPC\n");
					else
						io.print("EMPTY\n");
				}

				io.print("All disks initialized.\nFinding boot.basic\n");

				for(Character driveLetter : drives.navigableKeySet()) {
					String bootFileName = driveLetter + ":" + FileSystem.PATH_SEPARATOR + "boot.basic";
					io.print("Trying " + bootFileName);
					printLoad('.', 3, 1000, " ");

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

					io.print("FOUND\n");

					BaseCompiledProgram program;

					try {
						CodeParser parser = new CodeParser(new DriveGroupBasicFS(driveGroup), bootFileName, true);
						program = parser.compile();
					} catch (Exception e) {
						io.print("LOAD ERROR. Trying next.\n");
						e.printStackTrace();
						continue;
					}

					io.print("COMPILE OK. Invoking");
					printLoad('.', 3, 1000, "\n");

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
				if(promptCharInput == null) {
					promptCharInput = (keyChar == 0) ? (keyCode | 0x80000000) : keyChar;
					return;
				}

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

				GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
				synchronized (invertLock) {
					GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
					GL11.glColor4f(0.0f, 1.0f, 0.0f, 0.0f);
					GL11.glBegin(GL11.GL_QUADS);
					for (int i = 0; i < screenInvertX.length; i++) {
						int invX = screenInvertX[i];
						int invY = screenInvertY[i];
						GL11.glVertex3f(21 * invX, 32 * invY, 0.0f);
						GL11.glVertex3f(21 * (invX + 1), 32 * invY, 0.0f);
						GL11.glVertex3f(21 * (invX + 1), 32 * (invY + 1), 0.0f);
						GL11.glVertex3f(21 * invX, 32 * (invY + 1), 0.0f);
					}
					GL11.glEnd();
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				}

				for(MainShader shader : shaders)
					OpenGLMain.flipBuffers(shader);
			}
		});
	}
}
