// Block vertex shader — emulates OpenGL 1.x fixed-function pipeline.
// Supports: position, colour, texture coordinate, fog.
#ifdef GL_ES
precision mediump float;
#endif

attribute vec3 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projViewMatrix;
uniform int  u_hasTexture;   // 1 if textured, 0 otherwise
uniform int  u_hasColor;     // 1 if per-vertex colour, 0 otherwise
uniform vec4 u_color;        // global colour when per-vertex is off
uniform int  u_fogEnabled;   // 1 if fog on
uniform float u_fogDensity;

varying vec4 v_color;
varying vec2 v_texCoord;
varying float v_fogFactor;

void main() {
    gl_Position = u_projViewMatrix * vec4(a_position, 1.0);

    // Colour
    v_color = (u_hasColor == 1) ? a_color : u_color;

    // Texture coordinate
    v_texCoord = a_texCoord0;

    // Fog (EXP2) — distance from camera is approximated by clip-space Z
    if (u_fogEnabled == 1) {
        float dist = length(gl_Position.xyz);
        float f = u_fogDensity * dist;
        v_fogFactor = clamp(exp(-f * f), 0.0, 1.0);
    } else {
        v_fogFactor = 1.0;
    }
}
