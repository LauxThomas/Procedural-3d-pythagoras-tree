#version 420

layout(triangles) in;
layout(triangle_strip, max_vertices = 27) out;

in Vertex{
    vec3 vposition;
    float vlength;
    vec3 vnormal;
}vertex[];

out vec3 out_position;
out float out_length;
out vec3 out_normal;

//shrinkfactor
float shrinkFactor = 0.7f;

void calculateTriangle(vec3 vertex1, vec3 vertex2, vec3 vertex3, float extrudeLength);
void emitTriangle(vec3 vertex1, vec3 vertex2, vec3 vertex3, float extrudeLength, vec3 normal);

void main() {

    vec3 triangleVertices[3];
    for (int i = 0; i < 3; i++) {
        triangleVertices[i] = vertex[i].vposition;
    }
    float extrudeLength = vertex[0].vlength;

    if (extrudeLength == 0.0f) {
        //do not extrude:
        emitTriangle(triangleVertices[0], triangleVertices[1], triangleVertices[2], 0.0f, vertex[0].vnormal);
    }
    else {
        vec3 shrunkenTriangle[3];
        vec3 tipOfTetraeder;

        vec3 kath1 = triangleVertices[1] - triangleVertices[0];
        vec3 kath2 = triangleVertices[2] - triangleVertices[0];
        vec3 normal = normalize(cross(kath1, kath2));
        vec3 heigth = normal * extrudeLength;
        vec3 center = (triangleVertices[0]+triangleVertices[1]+triangleVertices[2])/3;

        for (int i = 0; i < 3; i++) {
            shrunkenTriangle[i] = center +  heigth + ((triangleVertices[i] - center) * shrinkFactor);
        }

        float tetraederAngle = shrinkFactor/3*length(kath1);
        tipOfTetraeder = center + heigth + normal * tetraederAngle;

        for (int i = 0; i < 3; i++) {
            calculateTriangle(triangleVertices[i], shrunkenTriangle[(i + 1) % 3], shrunkenTriangle[i], 0.0f);
            calculateTriangle(triangleVertices[i], triangleVertices[(i + 1) % 3], shrunkenTriangle[(i + 1) % 3], 0.0f);
        }

        for (int i = 0; i < 3; i++) {
            calculateTriangle(shrunkenTriangle[i], shrunkenTriangle[(i + 1) % 3], tipOfTetraeder, extrudeLength * shrinkFactor);
        }
    }
}

void calculateTriangle(vec3 vertex1, vec3 vertex2, vec3 vertex3, float extrudeLength) {
    emitTriangle(vertex1, vertex2, vertex3, extrudeLength, normalize(cross(vertex2-vertex1, vertex3-vertex1)));
}

void emitTriangle(vec3 vertex1, vec3 vertex2, vec3 vertex3, float extrudeLength, vec3 normal) {
    out_length = extrudeLength;
    out_normal = normal;
    out_position = vertex1;
    EmitVertex();
    out_position = vertex2;
    EmitVertex();
    out_position = vertex3;
    EmitVertex();
    EndPrimitive();
}

