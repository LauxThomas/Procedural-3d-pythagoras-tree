#version 330
    in vec3 pos;
    in vec3 normal;
    in vec3 color;
    in vec2 texcoord;
    in float length;
    uniform mat4 model;
    uniform mat4 view;
    uniform mat4 proj;
    out vec3 outColor;
    out vec2 outTexcoord;


    void main()
    {
        outColor=color;
        outTexcoord = texcoord;
        gl_Position =vec4(pos, 1.0);
    }