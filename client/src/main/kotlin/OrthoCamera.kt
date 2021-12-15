import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Mat4

class OrthoCamera(vararg programs : Program) : UniformProvider("camera") {

  val position = Vec2(0.0f, 0.0f) 
  val roll = 0.0f 
  val windowSize = Vec2(2.0f, 2.0f) 
    
  val viewProjMatrix by Mat4()
  val viewProjMatrixInverse by Mat4()  
  init{
    updateViewProjMatrix()
    addComponentsAndGatherUniforms(*programs)
  }

  fun updateViewProjMatrix() {
    viewProjMatrixInverse.set(). 
      scale(0.5f, 0.5f). 
      scale(windowSize). 
      rotate(roll). 
      translate(position)

    viewProjMatrix.set(). 
      scale(0.5f, 0.5f). 
      scale(windowSize). 
      rotate(roll). 
      translate(position). 
      invert()
  }

  fun setAspectRatio(ar : Float) { 
    windowSize.x = windowSize.y * ar
    updateViewProjMatrix()
  } 
}