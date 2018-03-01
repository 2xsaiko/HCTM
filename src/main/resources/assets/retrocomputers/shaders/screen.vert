#version 130

#extension GL_EXT_gpu_shader4: enable

uniform vec2 size;

out vec2 vertPos;
out vec2 texc;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    vertPos = gl_Vertex.xy;
    texc = vec2(gl_Vertex.x / size.x, 1 - (gl_Vertex.y / size.y));
}