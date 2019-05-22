#version 330
in vec3 position;
in vec3 normal;
in vec3 color;
in vec2 texCoords;
in float length;
out geoValue{
    vec3 pos;
    vec3 normal;
    vec3 color;
    vec2 texCoords;
    float length;
} vs_out;


void main()
{
    vs_out.pos = position;
    vs_out.normal = normal;
    vs_out.color = color;
    vs_out.texCoords = texCoords;
    vs_out.length = length;
}