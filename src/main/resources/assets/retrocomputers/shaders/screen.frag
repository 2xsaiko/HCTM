#version 130

#extension GL_EXT_gpu_shader4: enable

uniform int falloffcenter[2];
uniform sampler2D tex;

in vec2 vertPos;
in vec2 texc;

out vec4 fragColor;

void main() {
    vec4 color = texture(tex, texc);
    float alpha = color.a;

    // CRT effect, yay!
    // scanlines
    if (int(vertPos.y) % 2 == 1) {
        alpha *= 0.8;
    }
    float falloff = vertPos.y - falloffcenter[0];
    float falloff2 = vertPos.y - falloffcenter[1];
    alpha *= min(1.0, falloff * falloff * 0.001 + 0.6);
    alpha *= min(1.0, falloff2 * falloff2 * 0.01 + 0.6);

    // loss of intensity towards the corners
    float centerx = texc.x * 2 - 1;
    float centery = texc.y * 2 - 1;
    alpha *= min(1.0, 0.75 / (centerx * centerx));
    alpha *= min(1.0, 0.75 / (centery * centery));

    fragColor = vec4(color.x, color.y, color.z, alpha);
}
