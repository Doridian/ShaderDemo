#version 130

uniform vec2 resolution;
uniform sampler2D backbuffer;
uniform float time;

out vec4 FragColor;

const float modSize = 8.0;

const float modSizeH = modSize / 2.0;
//const float modSizeH = 4.0;

void main(void)
{
	float offset = 0;
	if(mod(time, 5f) < 0.05f) {
		offset = mod((gl_FragCoord.y + mod(time * 2001f, 153f)) / 5f, 10f);
		if(offset > 5f)
			offset = (5f - offset);
	}

	vec2 vTexCoord = (gl_FragCoord.xy + vec2(offset,0f)) / resolution.xy;
	
	//float mulHF = mod(gl_FragCoord.y, modSize);
	float mulHT = mod(gl_FragCoord.y + (time * 15f), modSize);
	
	vec4 color = texture2D(backbuffer, vTexCoord);
	
	color *=  0.6;
	color += vec4(0f,sin(mulHT / modSizeH)*0.1f,0f,0f);
    
	FragColor = color;
}