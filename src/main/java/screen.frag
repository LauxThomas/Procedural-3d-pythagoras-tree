#version 150 core

in vec3 Color;
in vec2 Texcoord;

out vec4 outColor;

uniform sampler2D tex;
uniform vec3 triangleColor;

void main()
{
    outColor = texture(tex, Texcoord) * vec4(Color, 1.0) * vec4(triangleColor, 1.0);
//    outColor = vec4(Color, 1.0)+ vec4(0.2, 0.2, 0.2, 0.2);
}