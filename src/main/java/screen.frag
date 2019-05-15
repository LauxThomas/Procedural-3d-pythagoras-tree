#version 330 core
in vec3 fragmentColor;
in vec2 fragmentTextureCoord;
out vec4 FragColor;
uniform vec3 triangleColor;
uniform sampler2D tex;


void main()
{
//    FragColor = texture(tex, fragmentTextureCoord);
    FragColor = texture(tex, fragmentTextureCoord) * vec4(triangleColor, 1.0);
}

//in vec3 Color;
//in vec2 Texcoord;
//
//out vec4 outColor;
//
//
//void main()
//{
////    outColor = texture(tex, Texcoord) * vec4(Color, 1.0) * vec4(triangleColor, 1.0);    //color + texture
////    outColor = texture(tex, Texcoord) * vec4(triangleColor, 1.0);    //texture
//    outColor = vec4(Color, 1.0f)*vec4(triangleColor,1.0f);    //color
//}