package de.doridian.crtdemo.shader;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;

public class OpenGLMain {
	static int screenWidth = 670;
	static int screenHeight = 510;

	static int renderAreaWidth = 670;
	static int renderAreaHeight = 510;

	public static boolean doReinitialize = true;

	public static boolean isRunning = true;
	public static boolean shouldRun = true;

	static int fpsLimit = 0;
	static long minFrameTime = 0;

	public static String lastShaderError = null;

	public static void setFPSLimit(final int fps) {
		if(fps <= 0) {
			fpsLimit = 0;
			minFrameTime = 0;
		} else {
			fpsLimit = fps;
			minFrameTime = 1000000000L / fps;
		}
	}

	public static int getFPSLimit() {
		return fpsLimit;
	}

	private static Renderer renderer;

	public interface Renderer {
		public void initialize();
		public void keyPressed(int keyCode, char keyChar);
		public void renderFrame();
	}

	static MainShader currentProgram = null;

	public static void main(Renderer _renderer) {
		setFPSLimit(60);

		renderer = _renderer;

		isRunning = true;
		shouldRun = true;

		try {
			Display.setDisplayMode(new DisplayMode(screenWidth, screenHeight));
			Display.create();
			Display.setTitle("CRT");
			Display.setResizable(false);
		} catch(LWJGLException e) {
			e.printStackTrace();
		}

		initDisplay();

		screenProgram = new ScreenShader();
		currentProgram = new MainShader(ShaderProgram.VSH_DONOTHING, null, MainShader.FSH_DONOTHING);

		inputThread.start();

		long lastFrameSampleTime = System.nanoTime();
		long thisFrameTime;
		long frameCount = 0;
		long fps; long frameTimeTmpDiff;

		long lastShadedFrameTime = 0;

		while(shouldRun && !Display.isCloseRequested()) {
			if(doReinitialize || Display.wasResized()) {
				initDisplay();
				doReinitialize = false;
			}

			thisFrameTime = System.nanoTime();
			frameTimeTmpDiff = thisFrameTime - lastFrameSampleTime;
			frameCount++;
			if(frameTimeTmpDiff > 1000000000) {
				fps = (long)((1000000000.0f / frameTimeTmpDiff) * frameCount);
				frameCount = 0;
				lastFrameSampleTime = thisFrameTime;
				Display.setTitle("CRT (" + fps + " FPS)");
			}

			if(fpsLimit > 0 && fpsLimit < 60) {
				frameTimeTmpDiff = thisFrameTime - lastShadedFrameTime;
				if(frameTimeTmpDiff > minFrameTime) {
					draw(true);
					lastShadedFrameTime = thisFrameTime;
				} else {
					draw(false);
				}
			} else {
				draw(true);
			}
			Display.update();
			if(fpsLimit > 0) {
				if(fpsLimit < 60)
					Display.sync(60);
				else
					Display.sync(fpsLimit);
			}
		}

		try {
			runInputThread = false;
			inputThread.join();
		} catch (InterruptedException e) { }

		Display.destroy();

		isRunning = false;
	}

	private static void initDisplay() {
		GL20.glUseProgram(0);

		GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);

		screenWidth = Display.getWidth();
		screenHeight = Display.getHeight();

		renderAreaWidth = screenWidth;
		renderAreaHeight = screenHeight;

		resX = renderAreaWidth;
		resY = renderAreaHeight;
		startTime = System.currentTimeMillis();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);

		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClearDepth(1);

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);

		GL11.glViewport(0, 0, renderAreaWidth, renderAreaHeight);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, renderAreaWidth, renderAreaHeight, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		if(frontArrayBuffer != 0)
			GL15.glDeleteBuffers(frontArrayBuffer);
		if(backArrayBuffer != 0)
			GL15.glDeleteBuffers(backArrayBuffer);
		frontArrayBuffer = genBuffer(1.0f, 1.0f);
		backArrayBuffer = genBuffer(renderAreaWidth, renderAreaHeight);

		createRenderTargets();

		renderer.initialize();
	}

	static boolean runInputThread = true;

	static int genBuffer(float scaleX, float scaleY) {
		FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(12);
		float[] floats = new float[] { -scaleX, -scaleY, scaleX, -scaleY, -scaleX, scaleY, scaleX, -scaleY, scaleX, scaleY, -scaleX, scaleY };
		floatBuffer.put(floats);
		floatBuffer.flip();

		int retBuffer = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, retBuffer);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, floatBuffer, GL15.GL_STATIC_DRAW);

		return retBuffer;
	}

	private static Thread inputThread = new Thread() {
		public void run() {
			while(runInputThread) {
				parseInput();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) { }
			}
		}
	};

	private static void createRenderTargets() {
		if(frontTarget != null) frontTarget.delete();
		if(backTarget != null) backTarget.delete();
		frontTarget = new  RenderTarget(renderAreaWidth, renderAreaHeight);
		backTarget = new RenderTarget(renderAreaWidth, renderAreaHeight);
	}

	static RenderTarget frontTarget, backTarget;

	static ScreenShader screenProgram = null;

	static int frontArrayBuffer = 0;
	static int backArrayBuffer = 0;

	static long startTime = 0;

	static int resX = 0;
	static int resY = 0;

	private static void parseInput() {
		while(Keyboard.next()) {
			if(!Keyboard.getEventKeyState()) continue;
			renderer.keyPressed(Keyboard.getEventKey(), Keyboard.getEventCharacter());
		}
	}

	private static void draw(boolean mayShade) {
		if(mayShade) {
			//BACK
			GL20.glUseProgram(0);
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, backTarget.framebuffer);

			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

			GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			renderer.renderFrame();

			flipBuffers(currentProgram);
		}

		// Set uniforms for screen shader

		GL20.glUseProgram(screenProgram.getProgram());

		GL20.glUniform2f(screenProgram.uniformResolution, resX, resY);
		GL20.glUniform1i(screenProgram.uniformTexture, 1);

		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, backTarget.texture);

		// Render front buffer to screen
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, frontArrayBuffer);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
	}

	public static void flipBuffers(MainShader shaderProgram) {
		GL20.glUseProgram(shaderProgram.getProgram());

		GL20.glUniform2f(shaderProgram.uniformResolution, renderAreaWidth, renderAreaHeight);
		GL20.glUniform1i(shaderProgram.uniformBackbuffer, 0);
		GL20.glUniform1i(shaderProgram.uniformOriginal, 11);
		GL20.glUniform1f(shaderProgram.uniformTime, (System.currentTimeMillis() - startTime) / 1000.0f);

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, backTarget.texture);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frontTarget.framebuffer);

		// Render custom shader to front buffer
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

		RenderTarget tmp = frontTarget;
		frontTarget = backTarget;
		backTarget = tmp;
	}
}