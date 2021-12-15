import vision.gears.webglmath.*
import kotlin.math.*

class Car(vararg val mesh : Mesh, scene : Scene) : GameObject(*mesh) {
    val scene = scene
    //TODO: physical properties here
    var angularVelocity = 0f
    val angularMass = 1.0f
    val angularDrag = 3f
    var acceleration = Vec3(.01f)
    val drag = 1.0f
    val grip = 1.2f
    var drift = false
    var alive = true
    init {
        invMass = 3f
    }
    override fun move(
    dt : Float,
    t : Float,
    keysPressed : Set<String>,
    gameObjects : List<GameObject>
    ) : Boolean {
    //TODO: actual physics here
    angularVelocity *= exp(-angularDrag * dt)
    if (alive)
        roll += angularVelocity / angularMass * dt

    velocity *= exp(-drag * dt)
    val direction = Vec3(cos(roll), sin(roll))
    val directionOrth = Vec3(-direction.y, direction.x)
    val accelerationForwardComponent = direction * (direction.dot(acceleration)/direction.length().pow(2))
    val accelerationLateralComponent = directionOrth * (directionOrth.dot(acceleration)/directionOrth.length().pow(2))
    val velocityLateralComponent = directionOrth * (directionOrth.dot(velocity)/directionOrth.length().pow(2))

    velocity += acceleration * dt
    if (!drift) {
        var dirDiff = roll - atan2(velocity.y, velocity.x)
        if (dirDiff.isNaN())
            dirDiff = 0f
        velocity.set(Vec3(cos(dirDiff)*velocity.x - sin(dirDiff)*velocity.y,
            sin(dirDiff)*velocity.x + cos(dirDiff)*velocity.y))
        if (accelerationLateralComponent.length() > grip)
            drift = !drift
    } else if (velocityLateralComponent.length() < grip/5f) {
        drift = !drift
    }
    else {
        scene.gameObjects += object : GameObject(scene.smokeMesh) {
            override fun move(dt : Float,t : Float,keysPressed : Set<String>,gameObjects : List<GameObject>
                ) : Boolean {
                    scale *= exp(-.5f * dt)
                    return true
            }
        }.also{
            it.position.set(position)
        }
    }
    if (alive)
        position += velocity * dt
    return true
    }
    override fun control(dt : Float, keysPressed: Set<String>, colliders: List<GameObject>) : Boolean {
        acceleration.set()
        if ("w" in keysPressed) {
            val modelSpaceAcceleration = Vec3(17f, 0f, 0f)
            acceleration.set(
            Vec4(modelSpaceAcceleration, 0f) * modelMatrix
            )
        }
        if ("a" in keysPressed) {
            angularVelocity += 10f * dt
        }
        if ("d" in keysPressed) {
            angularVelocity -= 10f * dt
        }
        colliders.forEach {
            if (it != this) {
                val posDiff = this.position - it.position
                if (posDiff.length() < max(this.scale.length(), it.scale.length())) {
                    if (scene.challengeMode && alive) {
                        alive = false
                        scene.explode()
                    }
                    else {
                        drift = true
                        val normal = posDiff.normalize()
                        this.position += normal * .1f
                        
                        val relVelocity = this.velocity - it.velocity
                        val restitutionCoefficient = 0.7f
                        val impMag = normal.dot(relVelocity) / (this.invMass + it.invMass) * (1f + restitutionCoefficient)
                        this.velocity += (normal * (-impMag * this.invMass).toFloat())
                        it.velocity -= (normal * (-impMag * it.invMass).toFloat())
                    }
                }
            }
        }
        return true
    }
}