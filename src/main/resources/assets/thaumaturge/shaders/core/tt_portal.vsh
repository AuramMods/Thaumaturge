#version 150

#moj_import <fog.glsl>

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 IViewRotMat;
uniform int FogShape;

out vec3 viewRay;
out float vertexDistance;

void main() {
    vec4 eye = ModelViewMat * vec4(Position, 1.0);
    vec4 clip = ProjMat * eye;
    gl_Position = clip;

    float scaleX = length(vec3(ProjMat[0][0], ProjMat[1][0], ProjMat[2][0]));
    float scaleY = length(vec3(ProjMat[0][1], ProjMat[1][1], ProjMat[2][1]));
    vec3 steadyEye = vec3(clip.x / scaleX, clip.y / scaleY, -clip.w);
    viewRay = IViewRotMat * steadyEye;
    vertexDistance = fog_distance(mat4(1.0), IViewRotMat * eye.xyz, FogShape);
}
