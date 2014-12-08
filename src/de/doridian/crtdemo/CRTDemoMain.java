package de.doridian.crtdemo;

import de.doridian.crtdemo.shader.MainShader;
import de.doridian.crtdemo.shader.OpenGLMain;
import de.doridian.crtdemo.shader.ShaderProgram;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

public class CRTDemoMain extends OpenGLMain {
	static boolean promptInputAllowed = true;
	static String promptInput = "";
	static String promptPS = "> ";

	//32 rows, 16 lines
	//670x510 screen area

	public static void main(String[] args) throws Exception {
		OpenGLMain.main(new Renderer() {
			private AngelCodeFont font;
			private MainShader[] shaders;

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
				if(!promptInputAllowed)
					return;
				if(keyCode == Keyboard.KEY_BACK) {
					if(promptInput.length() > 0)
						promptInput = promptInput.substring(0, promptInput.length() - 1);
					return;
				}
				if(keyChar != 0) {
					if(promptInput.length() >= 32)
						return;
					promptInput += keyChar;
				}
			}

			@Override
			public void renderFrame() {
				org.newdawn.slick.opengl.TextureImpl.unbind();

				font.drawString(0, 0, promptPS + promptInput + "\u00DC", Color.green);

				for(int i = 0; i < shaders.length; i++) {
					OpenGLMain.flipBuffers(shaders[i]);
				}
			}
		});
	}
}
