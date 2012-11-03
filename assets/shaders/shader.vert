
// Hook to manipulate the coordinates of objects
// that use this vertex shader
uniform mat4 uProjMatrix;

// Texture Coordinates
attribute vec2 aTextureCoord;
varying   vec2 vTextureCoord;

uniform float uZoom;

attribute vec4 vPosition;

void main() {
    gl_Position = vPosition * uProjMatrix
                  * mat4(1.0/uZoom,0,0,0,
                         0,1.0/uZoom,0,0,
                         0,0,1,0,
                         0,0,0,1);
    //gl_Position = vPosition;
    //vTextureCoord = gl_MultiTexCoord0;
}
