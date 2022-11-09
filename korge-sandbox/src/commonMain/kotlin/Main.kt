import com.soywiz.korge.Korge
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.scene.sceneContainer
import com.soywiz.korge.ui.uiCheckBox
import com.soywiz.korge.ui.uiComboBox
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.alignLeftToRightOf
import com.soywiz.korge.view.xy
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.mix
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.lang.portableSimpleName
import samples.*
import samples.asteroids.MainAsteroids
import samples.connect4.MainConnect4
import samples.minesweeper.MainMineSweeper
import samples.pong.MainPong
import samples.rpg.MainRpgScene

val DEFAULT_KORGE_BG_COLOR = Colors.DARKCYAN.mix(Colors.BLACK, 0.8)

suspend fun main() = Korge(
    bgcolor = DEFAULT_KORGE_BG_COLOR,
    clipBorders = false,
    //scaleMode = ScaleMode.EXACT,
    debug = true,
    multithreaded = true,
    forceRenderEveryFrame = false // Newly added optimization!
    //debugAg = true,
) {
    //uiButton("HELLO WORLD!", width = 300.0).position(100, 100); return@Korge
    //solidRect(100, 100, Colors.RED).xy(300, 300).filters(BlurFilter()); return@Korge

    demoSelector(
        //Demo(::MainJSMpeg),
        //Demo(::MainGraphicsText),
        //Demo(::MainTextBounds),
        //Demo(::MainEditor),
        //Demo(::MainStage3d),
        Demo(::MainInput),
        //Demo(::MainSvgAnimation),
        //Demo(::MainVectorNinePatch),
        listOf(
            Demo(::MainVectorNinePatch),
            Demo(::MainGraphicsText),
            Demo(::MainRpgScene),
            Demo(::MainJSMpeg),
            Demo(::MainSDF),
            Demo(::MainTextInput),
            Demo(::MainBlending),
            Demo(::MainXM),
            Demo(::MainVector),
            Demo(::MainText),
            Demo(::MainAtlas),
            Demo(::MainBunnysSlow),
            Demo(::MainOnScreenController),
            Demo(::MainScenes),
            Demo(::MainKTree),
            Demo(::MainLipSync),
            Demo(::MainInput),
            Demo(::MainGestures),
            Demo(::MainFilters),
            Demo(::MainCoroutine),
            Demo(::MainVideo),
            Demo(::MainPong),
            Demo(::MainUI),
            Demo(::MainLua),
            Demo(::MainOldMask),
            Demo(::MainNinePatch),
            Demo(::MainTweens),
            Demo(::MainTriangulation),
            Demo(::MainShapes),
            Demo(::MainSpriteAnim),
            Demo(::MainMineSweeper),
            Demo(::MainHelloWorld),
            Demo(::MainFlag),
            Demo(::MainAsteroids),
            Demo(::MainEmojiColrv1),
            Demo(::MainRotatedAtlas),
            Demo(::MainConnect4),
            Demo(::MainMutableAtlasTest),
            Demo(::MainTerminalEmulator),
            Demo(::MainParticles),
            Demo(::MainBezierSample),
            Demo(::MainEditor),
            Demo(::MainAnimations),
            Demo(::MainBunnymark),
            Demo(::MainKorviSample),
            Demo(::MainFiltersSample),
            Demo(::MainTextMetrics),
            Demo(::MainRenderText),
            Demo(::MainVectorRendering),
            Demo(::MainFilterScale),
            Demo(::MainExifTest),
            Demo(::MainColorTransformFilter),
            Demo(::MainMipmaps),
            Demo(::MainBmpFont),
            Demo(::MainPolyphonic),
            Demo(::MainSprites10k),
            Demo(::MainCustomSolidRectShader),
            Demo(::MainStage3d),
            Demo(::MainBlur),
            Demo(::MainFiltersRenderToBitmap),
            Demo(::MainColorPicker),
            Demo(::MainMasks),
            Demo(::MainHaptic),
            Demo(::MainSkybox),
            Demo(::MainDraggable),
            Demo(::MainGifAnimation),
            Demo(::MainTransition),
            Demo(::MainTilemapTest),
            Demo(::MainTextureIssue),
            Demo(::MainClipping),
            Demo(::MainTweenPoint),
            Demo(::MainEasing),
            Demo(::MainVectorFill),
            Demo(::MainFilterSwitch),
            Demo(::MainSvgAnimation),
            Demo(::MainArc),
            Demo(::MainStrokesExperiment),
            Demo(::MainStrokesExperiment2),
            Demo(::MainStrokesExperiment3),
            Demo(::MainZIndex),
            Demo(::MainDpi),
            Demo(::MainBezier),
            Demo(::MainUITreeView),
            Demo(::MainUIImageTester),
            Demo(::MainVampire),
            Demo(::MainCircles),
            Demo(::MainEmoji),
            Demo(::MainBVH),
            Demo(::MainImageTrace),
            Demo(::MainRotateCircle),
            Demo(::MainTrimmedAtlas),
            Demo(::MainRotatedTexture),
            Demo(::MainCircleColor),
            Demo(::MainGpuVectorRendering),
            Demo(::MainGpuVectorRendering2),
            Demo(::MainGpuVectorRendering3),
            Demo(::MainSound),
            Demo(::MainTilemapWithScroll),
            Demo(::MainTiledBackground),
            Demo(::MainAseprite),
            Demo(::MainAseParallaxSample),
        )
    )
}

class Demo(val sceneBuilder: () -> Scene, val name: String = sceneBuilder()::class.portableSimpleName.removePrefix("Main")) {
    override fun toString(): String = name
}

suspend fun Stage.demoSelector(default: Demo, all: List<Demo>) {
    val container = sceneContainer(width = width, height = height - 32.0) { }.xy(0, 32)

    suspend fun setDemo(demo: Demo?) {
        //container.removeChildren()
        if (demo != null) {
            views.clearColor = DEFAULT_KORGE_BG_COLOR
            container.changeTo({ injector ->
                demo.sceneBuilder().also { it.init(injector) }
            })
        }
    }

    val comboBox = uiComboBox(width = 300.0, items = (listOf(default) + all).distinctBy { it.name }.sortedBy { it.name }) {
        this.viewportHeight = 600
        this.onSelectionUpdate.add {
            println(it)
            launchImmediately { setDemo(it.selectedItem!!) }
        }
    }
    uiCheckBox(width = 300.0, text = "forceRenderEveryFrame", checked = views.forceRenderEveryFrame) {
        //x = 300.0
        alignLeftToRightOf(comboBox, padding = 16.0)
        //alignRightToLeftOf(comboBox)
        onChange {
            views.forceRenderEveryFrame = it.checked
        }
    }
    setDemo(default)
}
