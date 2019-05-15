#version 330
    in vec3 position;
    in vec3 color;
    in vec2 texcoord;
    uniform mat4 model;
    uniform mat4 view;
    uniform mat4 proj;
    out vec3 outColor;
    out vec2 outTexcoord;


    void main()
    {
        outColor=color;
        outTexcoord = texcoord;
        gl_Position =vec4(position, 1.0);
    }