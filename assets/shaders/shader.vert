uniform mat4 uProjMatrix;
uniform float uZoom;

attribute vec2 aTextureCoord;
attribute vec4 aVertexPosition;

varying vec2 vTextureCoord;

void main() {
	float s = 1.0/uZoom;
    gl_Position = aVertexPosition * uProjMatrix
                  * mat4(s,0,0,0,
                         0,s,0,0,
                         0,0,1,0,
                         0,0,0,1);
    vTextureCoord = aTextureCoord;
}
