package korlibs.image.format

import korlibs.datastructure.Extra
import korlibs.datastructure.toMap
import korlibs.math.geom.*

open class ImageInfo(
    val width: Int = 0,
    val height: Int = 0,
    val bitsPerPixel: Int = 8
) : Sizeable, Extra by Extra.Mixin() {
	override val size: Size get() = Size(width, height)

	override fun toString(): String = "ImageInfo(width=$width, height=$height, bpp=$bitsPerPixel, extra=${extra?.toMap()})"
}

fun ImageInfo(block: ImageInfo.() -> Unit): ImageInfo = ImageInfo().apply(block)
