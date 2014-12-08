package de.doridian.crtdemo;

import de.doridian.crtdemo.basic.BaseCompiledProgram;
import de.doridian.crtdemo.basic.BasicFunctions;
import de.doridian.crtdemo.basic.BasicIO;
import de.doridian.crtdemo.basic.CodeParser;
import de.doridian.crtdemo.shader.MainShader;
import de.doridian.crtdemo.shader.OpenGLMain;
import de.doridian.crtdemo.shader.ShaderProgram;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

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
		if(posY >= 16)
			doScrollUp();
		screenCursorOff[posY][posX] = c;
		screenCursorOn[posY][posX] = c;
		refreshCursor();
	}

	public static void nextLine() {
		posX = 32;
		moveForward();
	}

	public static void moveForward() {
		if(++posX >= 32) {
			posX %= 32;
			++posY;
		}
		refreshCursor();
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
		posY = 15;
		for(int i = 1; i < 16; i++) {
			screenCursorOff[i - 1] = screenCursorOff[i];
			System.arraycopy(screenCursorOff[i], 0, screenCursorOn[i - 1], 0, 32);
		}
		blankLine(15);
		refreshCursor();
	}

	public static class CRTBasicIO implements BasicIO {
		@Override
		public String getLine() {
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
				if(c == '\r' || c == '\n') {
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
	}

	public static void main(String[] args) throws Exception {
		for(int i = 0; i < 16; i++)
			blankLine(i);

		refreshCursor();

		CodeParser parser = new CodeParser(Util.readFile("data/tmp/test.basic"), true);
		final BaseCompiledProgram program = parser.compile();
		Thread basicThread = new Thread() {
			public void run() {
				BasicIO io = new CRTBasicIO();
				program.$start(io);
				nextLine();
				io.print("--- PROGRAM TERMINATED ---");
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
					font = new AngelCodeFont("data/greenscreen.fnt", new Image("data/greenscreen_0.png"), true);
				} catch (Exception e) {
					e.printStackTrace();
				}

				shaders = new MainShader[] {
					new MainShader(ShaderProgram.VSH_DONOTHING, null, Util.readFile("data/scanlines.fsh")),
					new MainShader(ShaderProgram.VSH_DONOTHING, null, Util.readFile("data/gblur_h.fsh")),
					new MainShader(ShaderProgram.VSH_DONOTHING, null, Util.readFile("data/gblur_v.fsh"))
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
