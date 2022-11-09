package com.soywiz.korim.font

import com.soywiz.kds.FastArrayList
import com.soywiz.kmem.toIntCeil
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.bitmap.NativeImage
import com.soywiz.korim.bitmap.context2d
import com.soywiz.korim.bitmap.effect.BitmapEffect
import com.soywiz.korim.bitmap.effect.applyEffect
import com.soywiz.korim.paint.DefaultPaint
import com.soywiz.korim.paint.NonePaint
import com.soywiz.korim.paint.Paint
import com.soywiz.korim.paint.Stroke
import com.soywiz.korim.text.BoundBuilderTextRendererActions
import com.soywiz.korim.text.DefaultStringTextRenderer
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korim.text.TextRenderer
import com.soywiz.korim.text.TextRendererActions
import com.soywiz.korim.text.invoke
import com.soywiz.korim.text.measure
import com.soywiz.korim.vector.Context2d
import com.soywiz.korio.lang.WStringReader
import com.soywiz.korio.resources.Resourceable
import com.soywiz.korma.geom.BoundsBuilder
import com.soywiz.korma.geom.IPoint
import com.soywiz.korma.geom.Matrix
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.Rectangle
import com.soywiz.korma.geom.bezier.Bezier
import com.soywiz.korma.geom.bezier.Curves
import com.soywiz.korma.geom.bezier.toBezier
import com.soywiz.korma.geom.bounds
import com.soywiz.korma.geom.shape.buildVectorPath
import com.soywiz.korma.geom.vector.VectorPath
import com.soywiz.korma.geom.vector.add
import com.soywiz.korma.geom.vector.applyTransform
import com.soywiz.korma.geom.vector.getCurves
import com.soywiz.korma.geom.vector.rect

interface Font : Resourceable<Font> {
    override fun getOrNull() = this
    override suspend fun get() = this

    val name: String

    // Metrics
    fun getFontMetrics(size: Double, metrics: FontMetrics = FontMetrics()): FontMetrics
    fun getGlyphMetrics(
        size: Double,
        codePoint: Int,
        metrics: GlyphMetrics = GlyphMetrics(),
        reader: WStringReader? = null
    ): GlyphMetrics
    fun getKerning(size: Double, leftCodePoint: Int, rightCodePoint: Int): Double

    // Rendering
    // Returns true if it painted something
    fun renderGlyph(
        ctx: Context2d, size: Double, codePoint: Int, x: Double, y: Double, fill: Boolean?,
        metrics: GlyphMetrics, reader: WStringReader? = null,
        beforeDraw: (() -> Unit)? = null
    ): Boolean
}

data class TextToBitmapResult(
    val bmp: Bitmap,
    override val fmetrics: FontMetrics,
    override val metrics: TextMetrics,
    override val glyphs: List<PlacedGlyphMetrics>,
    override val glyphsPerLine: List<List<PlacedGlyphMetrics>>
) : BaseTextMetricsResult

data class TextMetricsResult(
    override var fmetrics: FontMetrics = FontMetrics(),
    override var metrics: TextMetrics = TextMetrics(),
    override var glyphs: List<PlacedGlyphMetrics> = emptyList(),
    override var glyphsPerLine: List<List<PlacedGlyphMetrics>> = emptyList(),
) : BaseTextMetricsResult

class MultiplePlacedGlyphMetrics {
    val glyphs: FastArrayList<PlacedGlyphMetrics> = FastArrayList()
    val glyphsPerLine: FastArrayList<FastArrayList<PlacedGlyphMetrics>> = FastArrayList()

    val size: Int get() = glyphs.size

    fun add(glyph: PlacedGlyphMetrics) {
        glyphs += glyph
        while (glyphsPerLine.size <= glyph.nline) glyphsPerLine.add(FastArrayList())
        glyphsPerLine[glyph.nline].add(glyph)
    }

    operator fun plusAssign(glyph: PlacedGlyphMetrics) = add(glyph)
}

data class PlacedGlyphMetrics constructor(
    val codePoint: Int,
    val x: Double,
    val y: Double,
    val metrics: GlyphMetrics,
    val fontMetrics: FontMetrics,
    val transform: Matrix,
    val index: Int,
    val nline: Int,
) {
    val boundsPath: VectorPath by lazy {
        buildVectorPath {
            this.optimize = false
            val rect = Rectangle().copyFrom(metrics.bounds)
            rect.y = -fontMetrics.ascent
            rect.height = fontMetrics.ascent - fontMetrics.descent

            //println("rect=$rect, ascent=${fontMetrics.ascent}, descent=${fontMetrics.descent}")

            //rect.y = -rect.y
            //rect.height = -rect.height
            //rect.y = -rect.y
            //rect.height = -rect.height
            rect(rect)
        }.applyTransform(Matrix().translate(x, y).premultiply(transform))
    }
    val boundsPathCurves: Curves by lazy {
        val curves = boundsPath.getCurves()
        if (curves.beziers.size != 4) {
            println("curves=${curves.beziers.size}")
        }
        curves
    }
    val caretStart: Bezier by lazy { boundsPathCurves.beziers[3].toLine().flipped().toBezier() }
    val caretEnd: Bezier by lazy { boundsPathCurves.beziers[1] }

    fun distToPath(x: Double, y: Double, startEnd: Boolean? = null): Double {
        if (boundsPath.containsPoint(x, y)) return 0.0

        val middle = when (startEnd) {
            true -> caretStart.get(0.5)
            false -> caretEnd.get(0.5)
            null -> Point.middle(caretStart.get(0.5), caretEnd.get(0.5))
        }
        return Point.distance(middle.x, middle.y, x, y)
    }

    fun distToPath(p: IPoint, startEnd: Boolean? = null): Double = distToPath(p.x, p.y, startEnd)
}

interface BaseTextMetricsResult {
    val fmetrics: FontMetrics
    val metrics: TextMetrics
    val glyphs: List<PlacedGlyphMetrics>
    val glyphsPerLine: List<List<PlacedGlyphMetrics>>
}

fun Font.renderGlyphToBitmap(
    size: Double, codePoint: Int, paint: Paint = DefaultPaint, fill: Boolean = true,
    effect: BitmapEffect? = null,
    border: Int = 1,
    nativeRendering: Boolean = true,
    reader: WStringReader? = null
): TextToBitmapResult {
    val font = this
    val fmetrics = getFontMetrics(size)
    val gmetrics = getGlyphMetrics(size, codePoint, reader = reader)
    val gx = -gmetrics.left
    val gy = gmetrics.height + gmetrics.top
    val border2 = border * 2
    val iwidth = gmetrics.width.toIntCeil() + border2
    val iheight = gmetrics.height.toIntCeil() + border2
    val image = if (nativeRendering) NativeImage(iwidth, iheight) else Bitmap32(iwidth, iheight, premultiplied = true)
    image.context2d {
        fillStyle = paint
        font.renderGlyph(this, size, codePoint, gx + border, gy + border, fill = true, metrics = gmetrics)
        if (fill) fill() else stroke()
    }
    val imageOut = image.toBMP32IfRequired().applyEffect(effect)
    val glyph = PlacedGlyphMetrics(codePoint, gx + border, gy + border, gmetrics, fmetrics, Matrix(), 0, 0)
    return TextToBitmapResult(imageOut, fmetrics, TextMetrics(), listOf(glyph), listOf(listOf(glyph)))
}

// @TODO: Fix metrics
fun <T> Font.renderTextToBitmap(
    size: Double,
    text: T,
    paint: Paint = DefaultPaint,
    background: Paint = NonePaint,
    fill: Boolean = true,
    border: Int = 0,
    renderer: TextRenderer<T> = DefaultStringTextRenderer as TextRenderer<T>,
    returnGlyphs: Boolean = true,
    nativeRendering: Boolean = true,
    drawBorder: Boolean = false
): TextToBitmapResult {
    val font = this
    val bounds = getTextBounds(size, text, renderer = renderer)
    //println("BOUNDS: $bounds")
    val glyphs = MultiplePlacedGlyphMetrics()
    val iwidth = bounds.width.toIntCeil() + border * 2 + 1
    //println("bounds.nlines=${bounds.nlines}, bounds.allLineHeight=${bounds.allLineHeight}")
    val iheight = (if (drawBorder) bounds.allLineHeight else bounds.height).toIntCeil() + border * 2 + 1
    val image = if (nativeRendering) NativeImage(iwidth, iheight) else Bitmap32(iwidth, iheight, premultiplied = true)
    //println("bounds.firstLineBounds: ${bounds.firstLineBounds}")
    //println("bounds.bounds: ${bounds.bounds}")
    image.context2d {
        if (background != NonePaint) {
            this.fillStyle(background) {
                fillRect(0, 0, iwidth, iheight)
            }
        }
        //font.drawText(this, size, text, paint, bounds.drawLeft, bounds.drawTop, fill, renderer = renderer, placed = { codePoint, x, y, size, metrics, transform ->
        var index = 0
        font.drawText(this, size, text, paint, bounds.drawLeft, bounds.ascent, fill, renderer = renderer, placed = { codePoint, x, y, size, metrics, fmetrics, transform ->
            if (returnGlyphs) {
                glyphs += PlacedGlyphMetrics(codePoint, x, y, metrics.clone(), fmetrics, transform.clone(), index++, currentLineNum)
            }
        })
    }
    return TextToBitmapResult(image, bounds.fontMetrics, bounds, glyphs.glyphs, glyphs.glyphsPerLine)
}

@Deprecated("", ReplaceWith("getTextBoundsWithGlyphs(size, text, renderer, align)"))
fun <T> Font.measureTextGlyphs(
    size: Double,
    text: T,
    renderer: TextRenderer<T> = DefaultStringTextRenderer as TextRenderer<T>,
    align: TextAlignment = TextAlignment.BASELINE_LEFT
): TextMetricsResult = getTextBoundsWithGlyphs(size, text, renderer, align)

fun <T> Font.drawText(
    ctx: Context2d?,
    size: Double,
    text: T,
    paint: Paint?, // Deprecated parameter
    x: Double = 0.0, y: Double = 0.0,
    fill: Boolean = true, // Deprecated parameter
    renderer: TextRenderer<T> = DefaultStringTextRenderer as TextRenderer<T>,
    align: TextAlignment = TextAlignment.BASELINE_LEFT,
    outMetrics: TextMetricsResult? = null,

    fillStyle: Paint? = null,
    stroke: Stroke? = null,

    placed: (TextRendererActions.(codePoint: Int, x: Double, y: Double, size: Double, metrics: GlyphMetrics, fmetrics: FontMetrics, transform: Matrix) -> Unit)? = null
): TextMetricsResult? {
    //println("drawText!!: text=$text, align=$align")
    val glyphs = if (outMetrics != null) MultiplePlacedGlyphMetrics() else null
    val fnt = this
    //println("Font.drawText:")
    val doRender: () -> Unit = {
        //println("doRender=")
        if (fillStyle != null || stroke != null) {
            ctx?.fillStroke(fillStyle, stroke)
        } else {
            if (fill) ctx?.fill() else ctx?.stroke()
        }
        ctx?.beginPath()
    }
    val actions = object : TextRendererActions() {
        val metrics = renderer.measure(text, size, fnt)
        override fun put(reader: WStringReader, codePoint: Int): GlyphMetrics {
            val nline = currentLineNum
            val dx = metrics.getAlignX(align.horizontal, nline)
            val dy = metrics.getAlignY(align.vertical, fontMetrics)
            val px = this.x + x - dx
            val py = this.y + y + dy
            ctx?.keepTransform {
                //val m = getGlyphMetrics(codePoint)
                //println("valign=$valign, glyphMetrics.height=${glyphMetrics.height}")
                //println(glyphMetrics)
                //println(fontMetrics)
                //println(dy)
                ctx.translate(px, py)
                //ctx.translate(-m.width * transformAnchor.sx, +m.height * transformAnchor.sy)
                ctx.transform(this.transform)
                //ctx.translate(+m.width * transformAnchor.sx, -m.height * transformAnchor.sy)
                ctx.fillStyle = this.paint ?: paint ?: NonePaint
                this.font.renderGlyph(ctx, size, codePoint, 0.0, 0.0, null, glyphMetrics, reader, beforeDraw = doRender)
            }
            if (glyphs != null) {
                val glyph = PlacedGlyphMetrics(
                    codePoint, px, py, glyphMetrics.clone(), fontMetrics.clone(), transform.clone(),
                    glyphs.size, nline
                )
                glyphs.add(glyph)
            }
            placed?.invoke(this, codePoint, px, py, size, glyphMetrics, fontMetrics, this.transform)
            return glyphMetrics
        }
    }
    ctx?.beginPath()
    renderer.invoke(actions, text, size, this)
    doRender()
    if (outMetrics != null) {
        glyphs!!
        outMetrics.glyphs = glyphs.glyphs
        outMetrics.glyphsPerLine = glyphs.glyphsPerLine

        if (true) {
            val fmetrics = this.getFontMetrics(size)
            outMetrics.fmetrics = fmetrics
            val metrics = outMetrics.metrics
            metrics.fontMetrics.copyFrom(fmetrics)
            metrics.nlines = glyphs.glyphsPerLine.size
            metrics.lineBounds = glyphs.glyphsPerLine.map { glyphs ->
                val bb = BoundsBuilder()
                for (g in glyphs) {
                    bb.add(g.boundsPath)
                }
                bb.getBounds()
            }
            metrics.lineBounds.bounds(metrics.bounds)
        } else {
            val metrics = getTextBounds(size, text, renderer = renderer, align = align)
            outMetrics.fmetrics = metrics.fontMetrics
            outMetrics.metrics = metrics
        }
    }
    return outMetrics
}

fun <T> Font.getTextBoundsWithGlyphs(
    size: Double,
    text: T,
    renderer: TextRenderer<T> = DefaultStringTextRenderer as TextRenderer<T>,
    align: TextAlignment = TextAlignment.BASELINE_LEFT
): TextMetricsResult {
    val font = this
    //val bounds = getTextBounds(size, text, renderer = renderer)
    //println("BOUNDS: $bounds")
    return TextMetricsResult().also { out ->
        //font.drawText(null, size, text, null, bounds.drawLeft, bounds.ascent, false, renderer = renderer, outMetrics = out, align = align)
        font.drawText(null, size, text, null, 0.0, 0.0, false, renderer = renderer, outMetrics = out, align = align)
    }
}

fun <T> Font.getTextBounds(
    size: Double,
    text: T,
    out: TextMetrics = TextMetrics(),
    renderer: TextRenderer<T> = DefaultStringTextRenderer as TextRenderer<T>,
    align: TextAlignment = TextAlignment.TOP_LEFT
): TextMetrics {
    val actions = BoundBuilderTextRendererActions()
    renderer.invoke(actions, text, size, this)
    this.getFontMetrics(size, out.fontMetrics)
    out.nlines = actions.nlines

    // Compute
    val bb = BoundsBuilder()
    var dy = 0.0
    val lineBounds = FastArrayList<Rectangle>()
    val offsetY = actions.getAlignY(align.vertical, out.fontMetrics)
    //println("--")
    //println("offsetY=$offsetY, totalMaxLineHeight=${actions.totalMaxLineHeight}, align=$align")
    //printStackTrace()
    for (line in actions.lines) {
        val offsetX = line.getAlignX(align.horizontal)
        val rect = Rectangle(
            -offsetX,
            +offsetY + dy - out.ascent,
            line.maxX,
            line.maxLineHeight
        )
        //println("rect=$rect, offsetX=$offsetX, drawLeft=${out.drawLeft}")
        bb.add(rect)
        lineBounds.add(rect)
        dy += line.maxLineHeight
    }
    out.lineBounds = lineBounds
    bb.getBounds(out.bounds)

    return out
}
