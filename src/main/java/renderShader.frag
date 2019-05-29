#version 420
in float fraglength;
out vec4 FragColor;
void main ()
{
    FragColor  = vec4 (1.0f, 0.0f, fraglength, 0.3f);
}