#version 420
in float fraglength;
out vec4 frag_colour;
void main ()
{
    frag_colour = vec4 (0.0, 0.0, fraglength, 1.0);
}