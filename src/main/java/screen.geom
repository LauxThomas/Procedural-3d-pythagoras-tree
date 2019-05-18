#version 330 core
layout (triangles) in;
layout (triangle_strip, max_vertices = 32) out;

uniform mat4 model;
uniform mat4 view;
uniform mat4 proj;

in geoValue{
    vec3 pos;
    vec3 normal;
    vec3 color;
    vec3 texCoords;
    float length;
} gs_in[];
out DataBlock{
    vec3 pos;
    vec3 normal;
    vec3 color;
    vec3 texCoords;
    float length;
} outValue;

void passColorAndTexture(){
    outValue.color = gs_in[0].color;
    outValue.texCoords = gs_in[0].texCoords;
}

void constructTreeTrunk(vec4 position)
{
    vec4 v0 = proj*(view*(model*(position+ vec4(-1, 0, -1, 0))));//uhl
    vec4 v1 = proj*(view*(model*(position+ vec4(1, 0, -1, 0))));//uv
    vec4 v2 = proj*(view*(model*(position+ vec4(0, 0, 1, 0))));//uhr
    vec4 v3 = proj*(view*(model*(position+ vec4(-1, 2, -1, 0)*0.8)));//ohl
    vec4 v4 = proj*(view*(model*(position+ vec4(1, 2, -1, 0)*0.8)));//ov
    vec4 v5 = proj*(view*(model*(position+ vec4(0, 2, 1, 0)*0.8)));//ohr
    vec4 v6 = proj*(view*(model*(position+ vec4(0, 4, 0, 0)*0.6)));//tip

    //Bottom Pane
    gl_Position = v0;
    EmitVertex();
    gl_Position = v1;
    EmitVertex();
    gl_Position = v2;
    EmitVertex();
    EndPrimitive();

    //Top Pane
    gl_Position = v3;
    EmitVertex();
    gl_Position = v4;
    EmitVertex();
    gl_Position = v5;
    EmitVertex();
    EndPrimitive();

    //frontleft Pane1
    gl_Position = v0;
    EmitVertex();
    gl_Position = v1;
    EmitVertex();
    gl_Position = v4;
    EmitVertex();
    EndPrimitive();
    //frontleft Pane2
    gl_Position = v0;
    EmitVertex();
    gl_Position = v4;
    EmitVertex();
    gl_Position = v3;
    EmitVertex();
    EndPrimitive();

    //frontright Pane1
    gl_Position = v1;
    EmitVertex();
    gl_Position = v2;
    EmitVertex();
    gl_Position = v4;
    EmitVertex();
    EndPrimitive();
    //frontright Pane2
    gl_Position = v4;
    EmitVertex();
    gl_Position = v2;
    EmitVertex();
    gl_Position = v5;
    EmitVertex();
    EndPrimitive();

    //back Pane1
    gl_Position = v0;
    EmitVertex();
    gl_Position = v2;
    EmitVertex();
    gl_Position = v5;
    EmitVertex();
    EndPrimitive();
    //back Pane2
    gl_Position = v5;
    EmitVertex();
    gl_Position = v3;
    EmitVertex();
    gl_Position = v0;
    EmitVertex();
    EndPrimitive();

    //tip vl
    gl_Position = v3;
    EmitVertex();
    gl_Position = v4;
    EmitVertex();
    gl_Position = v6;
    EmitVertex();
    EndPrimitive();
    //tip vr
    gl_Position = v4;
    EmitVertex();
    gl_Position = v5;
    EmitVertex();
    gl_Position = v6;
    EmitVertex();
    EndPrimitive();
    //tip h
    gl_Position = v3;
    EmitVertex();
    gl_Position = v5;
    EmitVertex();
    gl_Position = v6;
    EmitVertex();
    EndPrimitive();


}



void constructNewTetraeder(){

}

void main() {
    passColorAndTexture();
    constructTreeTrunk(gl_in[0].gl_Position);//TODO: positionen zurückgeben und in vbo abspeichern. Dann rekursiv starten
    constructNewTetraeder();
}