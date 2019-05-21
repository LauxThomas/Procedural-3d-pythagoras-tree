#version 330

layout(location = 0) in vec3 position;

uniform mat4 model;
uniform mat4 view;
uniform mat4 proj;

out vec3 tPosition;

void main(){
    tPosition = position;
}