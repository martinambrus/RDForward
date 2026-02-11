// Block fragment shader — emulates OpenGL 1.x fixed-function pipeline.
// Supports: texture, colour, alpha blending, fog.
#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform int  u_hasTexture;
uniform int  u_fogEnabled;
uniform vec4 u_fogColor;

varying vec4 v_color;
varying vec2 v_texCoord;
varying float v_fogFactor;

void main() {
    vec4 texColor;
    if (u_hasTexture == 1) {
        texColor = texture2D(u_texture, v_texCoord) * v_color;
    } else {
        texColor = v_color;
    }

    // Fog blending
    if (u_fogEnabled == 1) {
        gl_FragColor = mix(u_fogColor, texColor, v_fogFactor);
    } else {
        gl_FragColor = texColor;
    }
}
