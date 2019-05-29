#version 420
in float fraglength;
out vec4 FragColor;
uniform sampler2D tex;
void main ()
{
    FragColor  = vec4 (0.5f, 0.5f, 0.5f, fraglength);
//    FragColor  =vec4 (1.0f, 0.0f, fraglength, 0.3f);    //works
}