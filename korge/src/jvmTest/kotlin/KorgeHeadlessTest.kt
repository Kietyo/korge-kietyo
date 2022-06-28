import com.soywiz.klock.Time
import com.soywiz.korge.Korge
import com.soywiz.korge.KorgeHeadless
import com.soywiz.korge.view.Views
import com.soywiz.korge.view.anchor
import com.soywiz.korge.view.image
import com.soywiz.korge.view.position
import com.soywiz.korge.view.renderToBitmap
import com.soywiz.korge.view.scale
import com.soywiz.korge.view.solidRect
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.BMP
import com.soywiz.korim.format.PNG
import com.soywiz.korim.format.readBitmap
import com.soywiz.korim.format.showImageAndWait
import com.soywiz.korim.format.writeBitmap
import com.soywiz.korio.async.suspendTest
import com.soywiz.korio.file.std.applicationVfs
import com.soywiz.korio.file.std.localCurrentDirVfs
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.file.std.rootLocalVfs
import com.soywiz.korio.file.std.standardVfs
import com.soywiz.korio.file.std.tempVfs
import com.soywiz.korma.geom.degrees
import kotlinx.coroutines.CoroutineScope
import org.junit.Test



class KorgeHeadlessTest {
//    @Test
//    //    @Ignore
//    fun test() = korgeTest {
//        System.setProperty("java.awt.headless", "false")
//        val gameWindow =
//            KorgeHeadless(width = 512, height = 512, bgcolor = Colors["#2b2b2b"], draw = true) {
//                val views = injector.get<Views>()
//                val minDegrees = (-16).degrees
//                val maxDegrees = (+16).degrees
//
//                val image = solidRect(100, 100, Colors.YELLOW) {
//                    rotation = maxDegrees
//                    anchor(.5, .5)
//                    scale(.8)
//                    position(256, 256)
//                }
//
//                println("applicationVfs: $applicationVfs")
//                println("localCurrentDirVfs: ${localCurrentDirVfs}")
//                println("rootLocalVfs: ${rootLocalVfs}")
//                println("tempVfs: ${tempVfs}")
//                println("standardVfs: ${standardVfs}")
//                println("applicationVfs[\"goldens\"]: ${applicationVfs["goldens"]}")
//
//                gameWindow.frameRender()
//
//                val currTime = System.currentTimeMillis()
//                while (true) {
//                    gameWindow.frameRender()
//                    if (System.currentTimeMillis() - currTime > 5000) {
//                        break
//                    }
//                }
//
//                //            val ss1 = renderToBitmap(views)
//                val ss1 = (gameWindow as KorgeHeadless.HeadlessGameWindow).bitmap
//                resourcesVfs["ss2.png"].writeBitmap(ss1, PNG)
//
//                //            while (true) {
//                //                println("STEP")
//                //                image.tween(image::rotation[minDegrees], time = 0.5.seconds, easing = Easing.EASE_IN_OUT)
//                //                image.tween(image::rotation[maxDegrees], time = 0.5.seconds, easing = Easing.EASE_IN_OUT)
//                //                views.gameWindow.close() // We close the window, finalizing the test here
//                //            }
//
//                views.gameWindow.close()
//            }
//        println("went here?!")
//        assert(true)
//    }

    @Test
    fun testDraw() = suspendTest {
        val gameWindow =
            KorgeHeadless(width = 512, height = 512, bgcolor = Colors["#2b2b2b"], draw = true) {
                val bmp = resourcesVfs["korge.png"].readBitmap()
                repeat(1) { n ->
                    //repeat(10) { n ->
                    //solidRect(512, 512, Colors.GREEN) {
                    image(bmp) {
                        //rotation = (45 + (n * 10)).degrees
                        rotation = (0 + (n * 10)).degrees
                        //rotation = 0.degrees
                        anchor(.5, .5)
                        scale(.5)
                        position(256, 256)
                        //position(256 + n * 10, 256 + n * 10)
                    }
                }

                //            try {
                //                bmp = stage.renderToBitmap(views)
                //            } finally {
                //            }

                val ss1 = stage.renderToBitmap(views)
                resourcesVfs["ss1.bmp"].writeBitmap(ss1, BMP)

                gameWindow.frameRender()

                val ss2 = stage.renderToBitmap(views)
                resourcesVfs["ss2.bmp"].writeBitmap(ss2, BMP)

                views.gameWindow.close() // We close the window, finalizing the test here
            }
        gameWindow.bitmap.showImageAndWait()
        println(gameWindow.bitmap)
    }
}
