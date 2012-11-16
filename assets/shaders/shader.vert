uniform mat4 uProjMatrix;
uniform float uZoom;
uniform vec2 uOffset;

attribute vec2 aTextureCoord;
attribute vec4 aVertexPosition;
varying vec2 vTextureCoord;

void main() {
	float x = uOffset.x;
	float y = uOffset.y;
	float s = uZoom;
    gl_Position = aVertexPosition * uProjMatrix
                  * mat4(s,0,0,x,
                         0,s,0,y,
                         0,0,1,0,
                         0,0,0,1);
    vTextureCoord = aTextureCoord;
}
