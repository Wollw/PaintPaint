precision mediump float;

uniform sampler2D uTexture;

varying vec2 vTextureCoord;

void main() {
    //gl_FragColor = vec4(0.5, 0.2, 0.5, 1.0);
    gl_FragColor = gl_FragCoord * 0.001;
    //gl_FragColor = texture2D(uTexture, vTextureCoord);
}
