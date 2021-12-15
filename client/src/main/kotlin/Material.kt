import vision.gears.webglmath.UniformProvider

class Material(program : Program) : UniformProvider("material") {
  init {
    addComponentsAndGatherUniforms(program)
  }
}
