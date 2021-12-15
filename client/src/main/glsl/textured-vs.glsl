#version 300 es

in vec4 vertexPosition; //#vec4# A four-element vector [x,y,z,w].; We leave z and w alone.; They will be useful later for 3D graphics and transformations. #vertexPosition# attribute fetched from vertex buffer according to input layout spec
in vec4 vertexTexCoord;

uniform struct{
  mat4 modelMatrix;
} gameObject;

uniform struct{
  mat4 viewProjMatrix; 
} camera;

uniform struct {
  float time;
} scene;

uniform struct {
  float denom;
  vec4 offset;
} animation;

out vec4 modelPosition;
out vec4 worldPosition;
out vec4 texCoord;

void main(void) {
  modelPosition = vertexPosition;
  gl_Position = vertexPosition * gameObject.modelMatrix * camera.viewProjMatrix;
  worldPosition = gl_Position;
  texCoord = vertexTexCoord/animation.denom + animation.offset;
}