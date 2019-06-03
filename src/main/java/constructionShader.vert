#version 420

layout(location = 0) in vec3 position;
layout(location = 1) in float length;
layout(location = 2) in vec3 normal;

out Vertex
{
    vec3 vertexPosition;
    float vertexLength;
    vec3 vertexNormal;
}vertex;


void main()
{
    vertex.vertexPosition = position;
    vertex.vertexLength = length;
    vertex.vertexNormal = normal;
}
