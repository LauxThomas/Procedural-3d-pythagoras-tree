#version 330

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

in vec3[] tPosition;

uniform mat4 model;
uniform mat4 view;
uniform mat4 proj;

out vec3 outPosition;


void main() {
    float border = 0.5f;
    //    if(pos.x > -border && pos.x < border && pos.y > -border && pos.y < border){
    outPosition = tPosition[0];
    EmitVertex();
    outPosition = tPosition[1];
    EmitVertex();
    outPosition = tPosition[2];
    EmitVertex();

    EndPrimitive();
    //    }
}
