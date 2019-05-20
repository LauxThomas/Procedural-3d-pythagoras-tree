#version 330

layout(points) in;
layout(points, max_vertices = 1) out;

in vec2[] tPosition;

out vec2 outPosition;

void main() {
    float border = 0.9f;
    vec2 pos = tPosition[0];
    if(pos.x > -border && pos.x < border && pos.y > -border && pos.y < border){
        outPosition = pos;
        EmitVertex();
        EndPrimitive();
    }
}