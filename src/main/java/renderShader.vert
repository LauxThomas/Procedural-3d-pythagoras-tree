#version 420
layout(location = 0) in vec3 position;
layout(location = 1) in float length;
uniform mat4 view;
uniform mat4 proj;
uniform mat4 model;
out float fraglength;
void main ()
{
    fraglength = length;
    gl_Position = proj * view * model * vec4 (position, 1.0);
}