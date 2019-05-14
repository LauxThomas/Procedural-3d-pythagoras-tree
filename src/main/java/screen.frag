#version 330 core
out vec4 FragColor;

void main()
{
    FragColor = vec4(1.0, 0.0, 0.5, 0.5);
}

//in vec3 Color;
//in vec2 Texcoord;
//
//out vec4 outColor;
//
//uniform sampler2D tex;
//uniform vec3 triangleColor;
//
//void main()
//{
////    outColor = texture(tex, Texcoord) * vec4(Color, 1.0) * vec4(triangleColor, 1.0);    //color + texture
////    outColor = texture(tex, Texcoord) * vec4(triangleColor, 1.0);    //texture
//    outColor = vec4(Color, 1.0f)*vec4(triangleColor,1.0f);    //color
//}