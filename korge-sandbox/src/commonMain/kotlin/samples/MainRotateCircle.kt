package samples

import com.soywiz.korev.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.vector.*

class MainRotateCircle : Scene() {
    override suspend fun SContainer.sceneMain() {
        //val circle = circle(radius = 50.0, fill = Colors.RED, stroke = Colors.BLUE, strokeThickness = 20.0).xy(0, 0).also {
        //val circle = circle(radius = 50.0, fill = Colors.RED, stroke = Colors.BLUE, strokeThickness = 20.0).xy(0, 0).centered.also {
        solidRect(300.0, 300.0, Colors.YELLOW).xy(250, 250).centered
        val circle = circle(radius = 150.0, fill = Colors.RED, stroke = Colors.BLUE, strokeThickness = 40.0).xy(250, 250).centered.also {
            //val circle = circle(radius = 50.0, fill = Colors.RED).xy(100, 100).centered.also {
            it.autoScaling = false
            //it.autoScaling = true
            //it.preciseAutoScaling = true
            //it.useNativeRendering = false
        }
        cpuGraphics({
            fill(Colors.PURPLE) {
                rect(-50, -50, 60, 60)
            }
        })
        stage!!.keys {
            downFrame(Key.LEFT) { circle.rotation -= 10.degrees }
            downFrame(Key.RIGHT) { circle.rotation += 10.degrees }
        }
    }
}
