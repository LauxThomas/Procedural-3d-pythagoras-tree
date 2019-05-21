#version 330

layout(points) in;
layout(points, max_vertices = 1) out;

in vec3[] tPosition;

uniform vec4 model;
uniform vec4 view;
uniform vec4 proj;

out vec3 outPosition;

void main() {
    float border = 0.5f;
    vec3 pos = tPosition[0];
    //    if(pos.x > -border && pos.x < border && pos.y > -border && pos.y < border){
    outPosition = pos+vec3(0.3,0.0,0.0);
    EmitVertex();
    EndPrimitive();
    //    }
}