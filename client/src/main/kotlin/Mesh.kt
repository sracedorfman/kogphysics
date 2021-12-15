import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Geometry

class Mesh(material : Material, geometry : Geometry) : UniformProvider("mesh") {

  init{
    addComponentsAndGatherUniforms(material, geometry)
  }
}