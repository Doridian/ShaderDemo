package de.doridian.crtdemo.shader;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;

public class ScreenShader extends ShaderProgram {
	public final int uniformResolution;
	public final int uniformTexture;

	public ScreenShader() {
		super(ShaderProgram.VSH_DONOTHING, null,
				"#version 130\n" +
				"uniform vec2 resolution;\n" +
				"uniform sampler2D backtexture;\n" +
				"out vec4 FragColor;\n" +
				"void main() {\n" +
				"	vec2 uv = gl_FragCoord.xy / resolution.xy;\n" +
				"	FragColor = texture(backtexture, uv);\n" +
				"}"
		);

		uniformResolution = getUniformLocation("resolution");
		uniformTexture = getUniformLocation("backtexture");
	}
}
