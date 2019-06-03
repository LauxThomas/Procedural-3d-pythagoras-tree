#version 420
in float renderAlpha;
out vec4 FragColor;
void main ()
{
    FragColor  = vec4 (0.1f, 0.1f, 0.1f, renderAlpha);
}