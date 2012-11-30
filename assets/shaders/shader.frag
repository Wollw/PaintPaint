precision mediump float;

uniform sampler2D uTexture;
uniform sampler2D uMask;

varying vec2 vTextureCoord;

void main() {
	vec4 color = texture2D(uTexture, vTextureCoord);
	float alpha = texture2D(uMask, vTextureCoord).a;
    if (alpha != 1.0)
		discard;        
    gl_FragColor = color;
}
