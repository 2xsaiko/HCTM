#version 130

#define SCREEN_WIDTH 80
#define SCREEN_HEIGHT 50

uniform usampler2D charset;
uniform usampler2D screen;

in vec2 uv1;

out vec4 fragColor;

void main() {
    vec2 scaled = vec2(uv1.x * SCREEN_WIDTH, uv1.y * SCREEN_HEIGHT);

    // where is this character on the screen? (0,0) - (SCREEN_WIDTH,SCREEN_HEIGHT)
    ivec2 char = ivec2(scaled);

    // which pixel of this character is this? (0,0)-(8,8)
    ivec2 chPixel = ivec2(scaled * 8) % 8;

    // which character is this? 0-255
    int chIndex = int(texelFetch(screen, char, 0).x);

    // the bitmap of the currently drawing line of the character
    int lineData = int(texelFetch(charset, ivec2(chPixel.y, chIndex), 0).x);
    int color = (lineData >> (7 - chPixel.x)) & 1;
    fragColor = vec4(color, color, color, 1);
}