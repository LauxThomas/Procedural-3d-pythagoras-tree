#version 420
in float fraglength;
out vec4 FragColor;
uniform sampler2D tex;
void main ()
{
    FragColor  = texture(tex, vec2(0.5, 0.2))*vec4 (0.5f, 0.5f, 0.5f, fraglength);
}