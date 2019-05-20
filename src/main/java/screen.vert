#version 330

layout(location = 0) in vec2 position;

out vec2 tPosition;

void main(){
    tPosition = position;
}