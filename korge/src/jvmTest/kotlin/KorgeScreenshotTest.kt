import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.korge.Korge
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.View
import com.soywiz.korge.view.Views
import com.soywiz.korge.view.anchor
import com.soywiz.korge.view.position
import com.soywiz.korge.view.renderToBitmap
import com.soywiz.korge.view.scale
import com.soywiz.korge.view.solidRect
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.PNG
import com.soywiz.korim.format.writeBitmap
import com.soywiz.korio.async.suspendTest
import com.soywiz.korio.file.std.localCurrentDirVfs
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.ISizeInt
import com.soywiz.korma.geom.degrees
import org.junit.Test

class KorgeTester(
    val views: Views,
    // If this is the first time we've seen the golden, then we will create it automatically.
    val createGoldenIfNotExists: Boolean = true
) {
    val testGoldensVfs = localCurrentDirVfs["testGoldens"]
    val localTestTime = DateTime.nowLocal()
    val tempVfs = localCurrentDirVfs["build/tmp/${PATH_DATE_FORMAT.format(localTestTime)}"]

    init {
        val currentTime = DATE_FORMAT.format(localTestTime)
        println("=".repeat(LINE_BREAK_WIDTH))
        println("Korge Tester initializing...")
        println("Local test time: $currentTime")
        println("Goldens directory: ${testGoldensVfs.absolutePath}")
        println("Temp directory: ${tempVfs.absolutePath}")
        println("=".repeat(LINE_BREAK_WIDTH))

        println(this::class)
        println(this::class.java)
        println(this::class.java.enclosingClass)
        //        println(this::class.java.getResource())
    }

    suspend fun init() {
        tempVfs.mkdirs()
    }

    // name: The name of the golden. (e.g: "cool_view").
    //  Note: Do not add an extension to the end.
    suspend fun recordGolden(view: View, name: String) {
        val bitmap = view.renderToBitmap(views)
        tempVfs["${name}.png"].writeBitmap(bitmap, PNG)
    }

    fun endTest() {
        views.gameWindow.close()
    }

    companion object {
        val DATE_FORMAT = DateFormat("yyyy-dd-MM HH:mm:ss z")
        val PATH_DATE_FORMAT = DateFormat("yyyyMMdd_HH_mm_ss_z")
        private val LINE_BREAK_WIDTH = 100
    }
}

inline fun Any.korgeTest(
    korgeConfig: Korge.Config,
    crossinline callback: suspend Stage.(korgeTester: KorgeTester) -> Unit
) {
    System.setProperty("java.awt.headless", "false")
    println(this::class)
    println(this::class.java)
    suspendTest {
        val korge = Korge(korgeConfig) {
            val korgeTester = KorgeTester(views)
            korgeTester.init()
            callback(this, korgeTester)
        }
    }
}

class KorgeScreenshotTest {
    @Test
    //    @Ignore
    fun test2() = korgeTest(
        Korge.Config(
            windowSize = ISizeInt.invoke(512, 512),
            virtualSize = ISizeInt(512, 512),
            bgcolor = Colors.RED
        )
    ) {
        //        val gameWindow = Korge(width = 512, height = 512, bgcolor = Colors.RED) {
        val views = injector.get<Views>()
        val minDegrees = (-16).degrees
        val maxDegrees = (+16).degrees

        val tester = KorgeTester(views)

        val image = solidRect(100, 100, Colors.YELLOW) {
            rotation = maxDegrees
            anchor(.5, .5)
            scale(.8)
            position(256, 256)
        }

        //            println("applicationVfs: $applicationVfs")
        //            println("localCurrentDirVfs: $localCurrentDirVfs")
        //            println("rootLocalVfs: $rootLocalVfs")
        //            println("tempVfs: $tempVfs")
        //            println("standardVfs: $standardVfs")
        //            println("applicationVfs[\"goldens\"]: ${applicationVfs["goldens"]}")
        //            val fileTest = localVfs(".")
        //            println("fileTest.absolutePath: ${fileTest.absolutePath}")
        //            val fileTest2 = localVfs("")
        //            println("fileTest2: ${fileTest2}")

//        val ss1 = renderToBitmap(views)
//        //                val ss1 = (gameWindow as KorgeHeadless.HeadlessGameWindow).bitmap
//        resourcesVfs["ss2.png"].writeBitmap(ss1, PNG)

        it.recordGolden(this, "initial")

        //            while (true) {
        //                println("STEP")
        //                image.tween(image::rotation[minDegrees], time = 0.5.seconds, easing = Easing.EASE_IN_OUT)
        //                image.tween(image::rotation[maxDegrees], time = 0.5.seconds, easing = Easing.EASE_IN_OUT)
        //                views.gameWindow.close() // We close the window, finalizing the test here
        //            }

        //                views.gameWindow.close()
        //        }

        it.endTest()
    }

}
