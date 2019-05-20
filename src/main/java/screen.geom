#version 330

layout(points) in;
layout(points, max_vertices = 1) out;

in vec2[] tPosition;

out vec2 outPosition;

void main() {
    vec2 pos = tPosition[0];
    if(pos.x > -0.5 && pos.x < 0.5 && pos.y > -0.5 && pos.y < 0.5){
        outPosition = pos;
        EmitVertex();
        EndPrimitive();
    }
}