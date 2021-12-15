import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL //# GL# we need this for the constants declared ˙HUN˙ a constansok miatt kell
import kotlin.js.Date
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec1
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec2Array
import vision.gears.webglmath.Vec3
import vision.gears.webglmath.Vec4
import vision.gears.webglmath.Mat4
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.PI

class Scene (
  val gl : WebGL2RenderingContext)  : UniformProvider("scene") {

  var challengeMode = false

  val vsTextured = Shader(gl, GL.VERTEX_SHADER, "textured-vs.glsl")
  val vsBackground = Shader(gl, GL.VERTEX_SHADER, "background-vs.glsl")  
  val fsTextured = Shader(gl, GL.FRAGMENT_SHADER, "textured-fs.glsl")
  val texturedProgram = Program(gl, vsTextured, fsTextured)
  val backgroundProgram = Program(gl, vsBackground, fsTextured)

  //TODO: create various materials with different solidColor settings
  val racerMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/car.png"))
  }
  val enemyMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/car2.png"))
  }
  val flameMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/flame.png"))
  }
  val rockMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/rock.png"))
  }
  val backgroundMaterial = Material(backgroundProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/racetrack.jpeg"))
  }
  val smokeMaterial = Material(texturedProgram).apply{
    this["colorTexture"]?.set(Texture2D(gl, "media/smoke.png"))
  }

  val texturedQuadGeometry = TexturedQuadGeometry(gl)
  val backgroundMesh = Mesh(backgroundMaterial, texturedQuadGeometry)
  val racerMesh = Mesh(racerMaterial, texturedQuadGeometry)
  val enemyMesh = Mesh(enemyMaterial, texturedQuadGeometry)
  val flameMesh = Mesh(flameMaterial, texturedQuadGeometry)
  val rockMesh = Mesh(rockMaterial, texturedQuadGeometry)
  val smokeMesh = Mesh(smokeMaterial, texturedQuadGeometry)
  
  val camera = OrthoCamera(*Program.all).apply{
    position.set(1f, 1f)
    windowSize.set(20f, 20f)
    updateViewProjMatrix()
  }

  val gameObjects = ArrayList<GameObject>()
  val colliders = ArrayList<GameObject>()


  val avatar = Car(racerMesh, scene=this).apply{
    position.set(23f, 45f)
    roll = PI.toFloat()
  }
  val flame = object : GameObject(flameMesh){
    var startTime = 0f
    override fun move(dt : Float,t : Float,keysPressed : Set<String>,gameObjects : List<GameObject>
      ) : Boolean {
        if (roll == 5f) {
          startTime = t
          roll = 4.64f
        }
        if (t - startTime < .2f)
          scale.set(1f,1f,1f)
        else if (t - startTime < .3f)
          scale.set(0f,0f,0f)
        else if (t - startTime < .4f)
          scale.set(1f,1f,1f)
        else
          scale.set(0f,0f,0f)
        return true
      }
  }.apply{
    parent = avatar
    roll = 4.64f
    position.set(-1.4f, .2f)
    scale.set(0f,0f,0f)
  }
  val enemy = CarAI(enemyMesh, scene=this).apply{
    position.set(23f, 43f)
    roll = PI.toFloat()
  }
  
  val denomHandle = gl.getUniformLocation(texturedProgram.glProgram, "animation.denom")
  init {
    gl.useProgram(texturedProgram.glProgram)
    gl.uniform1f(denomHandle, 1f)
    Vec4(0f,0f).commit(gl, gl.getUniformLocation(
      texturedProgram.glProgram,
      "animation.offset")!!)

    gameObjects += GameObject(backgroundMesh)
    gameObjects += flame

    gameObjects += GameObject(rockMesh).apply{
      invMass = 0f
      position.set(13f,12.5f)
      scale.set(2f,2f)
    }
    colliders += gameObjects.get(gameObjects.size-1)
    gameObjects += GameObject(rockMesh).apply{
      invMass = 0f
      position.set(39f,11.5f)
    }
    colliders += gameObjects.get(gameObjects.size-1)
    gameObjects += GameObject(rockMesh).apply{
      invMass = 0f
      position.set(38f,38f)
      scale.set(1.5f,1.5f)
    }
    colliders += gameObjects.get(gameObjects.size-1)
    gameObjects += GameObject(rockMesh).apply{
      invMass = 0f
      position.set(32f,29f)
      scale.set(1.5f,1.5f)
    }
    colliders += gameObjects.get(gameObjects.size-1)
    gameObjects += GameObject(rockMesh).apply{
      invMass = 0f
      position.set(26f,18.5f)
    }
    colliders += gameObjects.get(gameObjects.size-1)
    gameObjects += GameObject(rockMesh).apply{
      invMass = 0f
      position.set(18f,23f)
    }
    colliders += gameObjects.get(gameObjects.size-1)
    gameObjects += GameObject(rockMesh).apply{
      invMass = 0f
      position.set(14f,36f)
      scale.set(2f,2f)
    }
    colliders += gameObjects.get(gameObjects.size-1)

    gameObjects += enemy
    colliders += enemy

    gameObjects += avatar
    colliders += avatar
  }

  fun resize(canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)//#viewport# tell the rasterizer which part of the canvas to draw to ˙HUN˙ a raszterizáló ide rajzoljon
    camera.setAspectRatio(canvas.width.toFloat()/canvas.height)
  }

  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame =  timeAtFirstFrame
  //TODO: add property reflecting uniform scene.time
  //TODO: add all programs as child components

  fun flamePop() {
    if (avatar.velocity.length() > 15f && avatar.alive)
      flame.roll = 5f
  }

  fun explode() {
    racerMaterial["colorTexture"]?.set(Texture2D(gl, "media/boom.png"))
  }

  val boomOffset = Vec4(0f,0f)
  var boomCounter = 0

  @Suppress("UNUSED_PARAMETER")
  fun update(keysPressed : Set<String>) {
    val timeAtThisFrame = Date().getTime() 
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f
    //TODO: set property time (reflecting uniform scene.time) 
    timeAtLastFrame = timeAtThisFrame

    gl.clearColor(0.3f, 0.0f, 0.3f, 1.0f)//## red, green, blue, alpha in [0, 1]
    gl.clearDepth(1.0f)//## will be useful in 3D ˙HUN˙ 3D-ben lesz hasznos
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)//#or# bitwise OR of flags

    gl.enable(GL.BLEND)
    gl.blendFunc(
      GL.SRC_ALPHA,
      GL.ONE_MINUS_SRC_ALPHA)

    gameObjects.forEach{
      it.control(dt, keysPressed, colliders)
    }
    val removal = ArrayList<GameObject>()
    gameObjects.forEach{
      it.move(dt, t, keysPressed, gameObjects)
      if (it.scale.length() > 0f && it.scale.length() < 0.01f)
        removal.add(it)
    }
    removal.forEach{
      gameObjects.remove(it)
    }
    console.log(gameObjects.size)
    camera.position.set(avatar.position)
    camera.updateViewProjMatrix()
    gameObjects.forEach{
      it.update()
    }
    gameObjects.forEach{
      if (it != avatar)
        it.draw(this, camera)
    }
    if (avatar.alive)
      avatar.draw(this,camera)
    else {
      gl.uniform1f(denomHandle, 6f)
      boomOffset.commit(gl, gl.getUniformLocation(
        texturedProgram.glProgram,
        "animation.offset")!!)
      avatar.draw(this, camera)
      if (boomCounter >= 36) {
        avatar.position.set(23f,45f)
        avatar.roll = PI.toFloat()
        avatar.velocity.set(0f,0f)
        racerMaterial["colorTexture"]?.set(Texture2D(gl, "media/car.png"))
        avatar.alive = true
        boomOffset.set(0f,0f)
        boomCounter = 0
      } else {
        boomOffset.set(boomCounter%6*(1f/6f), boomCounter/6*(1f/6f))
        boomCounter++
      }
      gl.uniform1f(denomHandle, 1f)
      Vec4(0f,0f).commit(gl, gl.getUniformLocation(
        texturedProgram.glProgram,
        "animation.offset")!!)
    }
  }
}
