import vision.gears.webglmath.*
import kotlin.math.exp
import kotlin.math.PI
import kotlin.math.floor

open class GameObject(
  vararg val meshes : Mesh
   ) : UniformProvider("gameObject") {

  val position = Vec3()
  var roll = 0.0f
  val scale = Vec3(1.0f, 1.0f, 1.0f)
  val velocity = Vec3()
  var invMass = 1f

  val modelMatrix by Mat4()

  var parent : GameObject? = null

  init { 
    addComponentsAndGatherUniforms(*meshes)
  }

  fun update() {
    modelMatrix.set().
      scale(scale).
      rotate(roll).
      translate(position)
    parent?.let { parent ->
      modelMatrix *= parent.modelMatrix
    }
  }

  open fun move(
      dt : Float,
      t : Float,
      keysPressed : Set<String>,
      gameObjects : List<GameObject>
      ) : Boolean {
        velocity *= exp(-1f * dt)
        position += velocity * dt
    return true;
  }

  open fun control(
      dt : Float = 0.016666f,
      keysPressed : Set<String> = emptySet<String>(),
      colliders : List<GameObject> = emptyList<GameObject>()
      ) : Boolean {
    return true;
  }

}
