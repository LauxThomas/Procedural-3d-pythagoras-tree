#version 420
layout(location = 0) in vec3 position;
layout(location = 1) in float length;
layout(location = 2) in vec3 normal;
uniform mat4 view;
uniform mat4 proj;
uniform mat4 model;
mat4 mvp = proj * view * model;
uniform mat4 tv;
out float fraglength;
out float fragnormal;
void main ()
{
    fraglength = length;
//    fragnormal = normal.y;
    gl_Position = mvp * vec4 (position, 1.0);
//    gl_Position = vec4 (position, 1.0);
}