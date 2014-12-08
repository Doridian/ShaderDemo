#version 130

uniform vec2 resolution;
uniform sampler2D backbuffer;
out vec4 FragColor;
 
const float blurSize = 1.0/512.0; // I've chosen this size because this will result in that every step will be one pixel wide if the backbuffer texture is of size 512x512
 
void main(void)
{
	vec2 vTexCoord = gl_FragCoord.xy / resolution.xy;

	vec4 sum = vec4(0.0);

	// blur in y (vertical)
	// take nine samples, with the distance blurSize between them
	sum += texture2D(backbuffer, vec2(vTexCoord.x - 4.0*blurSize, vTexCoord.y)) * 0.05;
	sum += texture2D(backbuffer, vec2(vTexCoord.x - 3.0*blurSize, vTexCoord.y)) * 0.09;
	sum += texture2D(backbuffer, vec2(vTexCoord.x - 2.0*blurSize, vTexCoord.y)) * 0.12;
	sum += texture2D(backbuffer, vec2(vTexCoord.x - blurSize, vTexCoord.y)) * 0.15;
	sum += texture2D(backbuffer, vec2(vTexCoord.x, vTexCoord.y)) * 0.86;
	sum += texture2D(backbuffer, vec2(vTexCoord.x + blurSize, vTexCoord.y)) * 0.15;
	sum += texture2D(backbuffer, vec2(vTexCoord.x + 2.0*blurSize, vTexCoord.y)) * 0.12;
	sum += texture2D(backbuffer, vec2(vTexCoord.x + 3.0*blurSize, vTexCoord.y)) * 0.09;
	sum += texture2D(backbuffer, vec2(vTexCoord.x + 4.0*blurSize, vTexCoord.y)) * 0.05;

	FragColor = sum;
}