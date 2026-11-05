package de.bildgestalter

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ComposeShader
import android.graphics.ImageDecoder
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextPaint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

const val BASE = 1080f

enum class Mode { FREISTIL, COLLAGE, REIHE }

val FONT_NAMES = listOf("Russo One", "Serif", "Schreibmaschine", "Handschrift", "Fett", "Kursiv")

// Wird beim Start der App gesetzt (MainActivity)
var russoTypeface: Typeface? = null

fun typefaceOf(i: Int): Typeface = when (i) {
    0 -> russoTypeface ?: Typeface.SANS_SERIF
    1 -> Typeface.SERIF
    2 -> Typeface.MONOSPACE
    3 -> Typeface.create("cursive", Typeface.NORMAL)
    4 -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    else -> Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
}

/* ---------- Gold und Silber ---------- */

// Spezielle Farbwerte, die als Metall gezeichnet werden
val GOLD = Color(0xFFD4AF37)
val SILVER = Color(0xFFC0C0C0)

fun glitterKind(c: Color?): Int = when (c) {
    GOLD -> 1
    SILVER -> 2
    else -> 0
}

private const val TILE = 512
private const val GRAIN_UNITS = 1.4f
private val tileCache = HashMap<Int, Bitmap>()

private fun grainTile(kind: Int): Bitmap = synchronized(tileCache) {
    tileCache.getOrPut(kind) { makeTile(kind) }
}

// Sehr feines Korn (Gold) bzw. gebürstete Streifen (Silber), nahtlos kachelbar
private fun makeTile(kind: Int): Bitmap {
    val px = IntArray(TILE * TILE)
    val rnd = java.util.Random(if (kind == 1) 11L else 23L)
    if (kind == 1) {
        for (i in px.indices) {
            val v = rnd.nextGaussian().toFloat()
            val a = (min(1f, abs(v) * 0.4f) * 70f).toInt()
            px[i] = if (v >= 0f) android.graphics.Color.argb(a, 255, 244, 190)
            else android.graphics.Color.argb(a, 110, 70, 0)
        }
    } else {
        val tau = (2.0 * Math.PI).toFloat()
        val freq = floatArrayOf(3f, 8f, 21f)
        val amp = floatArrayOf(0.5f, 0.35f, 0.25f)
        for (y in 0 until TILE) {
            val rowV = rnd.nextGaussian().toFloat()
            val ph = FloatArray(3) { rnd.nextFloat() * tau }
            for (x in 0 until TILE) {
                var wv = 0f
                for (j in 0..2) wv += amp[j] * sin(tau * freq[j] * x / TILE + ph[j])
                val v = rowV * 0.9f + wv * 0.6f + rnd.nextGaussian().toFloat() * 0.2f
                val a = (min(1f, abs(v) * 0.3f) * 60f).toInt()
                px[y * TILE + x] = if (v >= 0f) android.graphics.Color.argb(a, 255, 255, 255)
                else android.graphics.Color.argb(a, 90, 90, 95)
            }
        }
    }
    return Bitmap.createBitmap(px, TILE, TILE, Bitmap.Config.ARGB_8888)
}

/** Metall-Verlauf aus einem Guss (w, h = Größe der Fläche, in der gezeichnet wird). */
fun glitterShader(kind: Int, w: Float, h: Float): Shader {
    val ww = max(1f, w)
    val hh = max(1f, h)
    val grad: Shader = if (kind == 1) {
        LinearGradient(
            0f, hh, ww, 0f,
            intArrayOf(
                0xFFB8842A.toInt(), 0xFFD6AB3B.toInt(), 0xFFEBD36B.toInt(),
                0xFFE3C75C.toInt(), 0xFFD3AA36.toInt()
            ),
            floatArrayOf(0f, 0.3f, 0.58f, 0.8f, 1f),
            Shader.TileMode.CLAMP
        )
    } else {
        LinearGradient(
            0f, 0f, 0f, hh,
            intArrayOf(0xFFB2B2B2.toInt(), 0xFFF2F2F2.toInt(), 0xFFD4D4D4.toInt(), 0xFFBCBCBC.toInt()),
            floatArrayOf(0f, 0.27f, 0.62f, 1f),
            Shader.TileMode.CLAMP
        )
    }
    val tile = BitmapShader(grainTile(kind), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    val m = Matrix()
    m.setScale(GRAIN_UNITS, GRAIN_UNITS)
    tile.setLocalMatrix(m)
    return ComposeShader(grad, tile, PorterDuff.Mode.SRC_OVER)
}

fun applyFill(p: Paint, c: Color, w: Float, h: Float) {
    val gk = glitterKind(c)
    if (gk == 0) {
        p.shader = null
        p.color = c.toArgb()
    } else {
        p.color = android.graphics.Color.WHITE
        p.isFilterBitmap = true
        p.shader = glitterShader(gk, w, h)
    }
}

/* ---------- Seitenverhältnisse ---------- */

class Ratio(val w: Int, val h: Int) {
    val label: String get() = "$w:$h"
}

val RATIOS = listOf(
    Ratio(1, 1), Ratio(4, 5), Ratio(3, 4), Ratio(2, 3), Ratio(9, 16), Ratio(1, 2),
    Ratio(5, 4), Ratio(4, 3), Ratio(3, 2), Ratio(16, 9), Ratio(2, 1)
)

/* ---------- Bildauflösung ---------- */

val QUALITY_NAMES = listOf("HD", "durchschnittlich", "sparsam")

// Längste Seite beim Export in Pixeln
val QUALITY_PX = floatArrayOf(5000f, 2500f, 1080f)

fun loadQuality(context: Context, mode: Mode): Int =
    context.getSharedPreferences("bildgestalter", Context.MODE_PRIVATE)
        .getInt("quality_" + mode.name, 0).coerceIn(0, 2)

fun saveQuality(context: Context, mode: Mode, q: Int) {
    context.getSharedPreferences("bildgestalter", Context.MODE_PRIVATE)
        .edit().putInt("quality_" + mode.name, q).apply()
}

/* ---------- Daten ---------- */

class Loaded(val bmp: Bitmap, val uri: Uri)

class ImgItem(val bmp: Bitmap, val uri: Uri?) {
    var x by mutableStateOf(0f)
    var y by mutableStateOf(0f)
    var w by mutableStateOf(500f)
    var h by mutableStateOf(500f)
    var rot by mutableStateOf(0f)        // Drehung in Grad (nur Freistil)
}

class TxtItem {
    var text by mutableStateOf("Text")
    var x by mutableStateOf(100f)        // linke obere Ecke des Textfeldes
    var y by mutableStateOf(100f)
    var fs by mutableStateOf(100f)       // Schriftgröße
    var font by mutableStateOf(0)
    var color by mutableStateOf(Color.Black)
    var bg by mutableStateOf<Color?>(null)
    var align by mutableStateOf(1)

    var pad by mutableStateOf(100)       // Hintergrundfläche 0..100
    var transp by mutableStateOf(0)      // Transparenz 0..100 (100 = unsichtbar)
    var corner by mutableStateOf(0)      // Abgerundete Ecken 0..100
    var lineSp by mutableStateOf(50)     // Zeilenabstand 0..100 (50 = normal)
    var letterSp by mutableStateOf(50)   // Buchstabenabstand 0..100 (50 = normal)
    var curve by mutableStateOf(0)       // Zeilenkrümmung -100..100
    var rot by mutableStateOf(0f)        // Drehung in Grad
}

class EditorState(val mode: Mode) {
    val images = mutableStateListOf<ImgItem>()
    val texts = mutableStateListOf<TxtItem>()
    var selected by mutableStateOf<Any?>(null)
    var vertical by mutableStateOf(true)
    var collageIdx by mutableStateOf(0)

    // Rahmen
    var frameOpen by mutableStateOf(false)
    var gap by mutableStateOf(0)          // "Dicke" 0..100
    var rundung by mutableStateOf(0)      // "Rundung" 0..100
    var frameColor by mutableStateOf(Color.White)

    // Format (Freistil und Collage), Standard 1:1
    var ratioIdx by mutableStateOf(0)

    // Bildauflösung: 0 = HD, 1 = durchschnittlich, 2 = sparsam
    var quality by mutableStateOf(0)
}

fun EditorState.select(item: Any?) {
    selected = item
    if (item is TxtItem) frameOpen = false
}

fun deleteItem(s: EditorState, item: Any) {
    when (item) {
        is ImgItem -> s.images.remove(item)
        is TxtItem -> s.texts.remove(item)
    }
    if (s.selected === item) s.selected = null
}

fun EditorState.addImages(list: List<Loaded>) {
    val (cw, ch) = canvasSize(this)
    for (l in list) {
        val b = l.bmp
        val item = ImgItem(b, l.uri)
        if (mode == Mode.FREISTIL) {
            val iw = b.width.toFloat()
            val ih = b.height.toFloat()
            if (images.isEmpty()) {
                // Erstes Bild füllt das Feld komplett (keine Leerflächen)
                val s = max(cw / iw, ch / ih)
                item.w = iw * s
                item.h = ih * s
                item.x = (cw - item.w) / 2f
                item.y = (ch - item.h) / 2f
            } else {
                var w = cw * 0.5f
                var h = w * ih / iw
                val maxH = ch * 0.7f
                if (h > maxH) {
                    w *= maxH / h
                    h = maxH
                }
                val off = (images.size % 6) * 40f
                item.w = w
                item.h = h
                item.x = min(cw * 0.1f + off, cw - w).coerceAtLeast(0f)
                item.y = min(ch * 0.1f + off, ch - h).coerceAtLeast(0f)
            }
        }
        images.add(item)
    }
}

fun clampTexts(s: EditorState) {
    val (w, h) = canvasSize(s)
    s.texts.forEach { t ->
        val g = textGeom(t)
        t.x = t.x.coerceIn(0f, max(0f, w - g.w))
        t.y = t.y.coerceIn(0f, max(0f, h - g.h))
    }
}

/* ---------- Text: Geometrie, Krümmung, Zeichnen ---------- */

class Pt(val x: Float, val y: Float)

/**
 * Verbiegt ein Rechteck (w x h) entlang eines Kreisbogens.
 * c > 0: Bogen nach oben, c < 0: Bogen nach unten, +-100 = Vollkreis.
 */
class Warp(val w: Float, val h: Float, val c: Int) {
    val on = c != 0
    private val theta = abs(c) / 100f * (2f * Math.PI.toFloat())
    val r = if (on) w / theta else 0f

    fun phi(u: Float): Float = if (on) (u - w / 2f) / r else 0f

    fun map(u: Float, v: Float): Pt {
        if (!on) return Pt(u, v)
        val p = phi(u)
        return if (c > 0) {
            val rho = max(0f, r + (h / 2f - v))
            Pt(w / 2f + rho * sin(p), h / 2f + r - rho * cos(p))
        } else {
            val rho = max(0f, r + (v - h / 2f))
            Pt(w / 2f + rho * sin(p), h / 2f - r + rho * cos(p))
        }
    }
}

/** Umriss eines Rechtecks mit gerundeten Ecken als Punktfolge (im Uhrzeigersinn). */
fun outlinePoints(w: Float, h: Float, rad: Float, n: Int): List<Pt> {
    val rr = min(rad, min(w, h) / 2f)
    val out = ArrayList<Pt>()
    for (i in 0..n) out.add(Pt(rr + (w - 2 * rr) * i / n, 0f))
    fun arc(cx: Float, cy: Float, a0: Float, a1: Float) {
        for (i in 1..8) {
            val a = Math.toRadians((a0 + (a1 - a0) * i / 8f).toDouble())
            out.add(Pt(cx + rr * cos(a).toFloat(), cy + rr * sin(a).toFloat()))
        }
    }
    arc(w - rr, rr, -90f, 0f)
    out.add(Pt(w, h - rr))
    arc(w - rr, h - rr, 0f, 90f)
    for (i in 1..n) out.add(Pt(w - rr - (w - 2 * rr) * i / n, h))
    arc(rr, h - rr, 90f, 180f)
    out.add(Pt(0f, rr))
    arc(rr, rr, 180f, 270f)
    return out
}

fun lineMult(p: Int): Float = if (p <= 50) 0.5f + p / 100f else 1f + (p - 50) / 50f

fun letterEm(p: Int): Float =
    if (p <= 50) (p - 50) / 50f * 0.1f else (p - 50) / 50f * 0.5f

class TextGeom(
    val w: Float, val h: Float, val rad: Float,
    val lines: List<String>, val lx: FloatArray, val ly: FloatArray,
    val paint: TextPaint,
    val bx0: Float, val by0: Float, val bx1: Float, val by1: Float,
    val path: Path?,
    val tl: Pt, val br: Pt, val bl: Pt,
    val margin: Float
)

/**
 * Berechnet das Textfeld: Die Größe richtet sich nach den eingegebenen Zeichen.
 * Hintergrundfläche 0 % = exakt der Rand der Zeichen, 100 % = eine "T"-Höhe darüber hinaus.
 */
fun textGeom(t: TxtItem): TextGeom {
    val tp = TextPaint(Paint.ANTI_ALIAS_FLAG)
    tp.textSize = t.fs
    tp.typeface = typefaceOf(t.font)
    tp.letterSpacing = letterEm(t.letterSp)
    tp.color = if (glitterKind(t.color) != 0) android.graphics.Color.BLACK else t.color.toArgb()

    val raw = if (t.text.isEmpty()) " " else t.text
    val lines = raw.split("\n")
    val fm = tp.fontMetrics
    val lh = (fm.descent - fm.ascent) * lineMult(t.lineSp)
    val rc = Rect()
    tp.getTextBounds("T", 0, 1, rc)
    val capH = max(1f, rc.height().toFloat())

    val adv = FloatArray(lines.size) { tp.measureText(lines[it]) }
    val maxAdv = adv.maxOrNull() ?: 0f
    val lx0 = FloatArray(lines.size) {
        when (t.align) {
            0 -> 0f
            2 -> maxAdv - adv[it]
            else -> (maxAdv - adv[it]) / 2f
        }
    }
    val ly0 = FloatArray(lines.size) { -fm.ascent + it * lh }

    var inkL = Float.MAX_VALUE
    var inkR = -Float.MAX_VALUE
    var inkT = Float.MAX_VALUE
    var inkB = -Float.MAX_VALUE
    var any = false
    for (i in lines.indices) {
        if (lines[i].isBlank()) continue
        tp.getTextBounds(lines[i], 0, lines[i].length, rc)
        any = true
        inkL = min(inkL, lx0[i] + rc.left)
        inkR = max(inkR, lx0[i] + rc.right)
        inkT = min(inkT, ly0[i] + rc.top)
        inkB = max(inkB, ly0[i] + rc.bottom)
    }
    if (!any) {
        inkL = 0f
        inkR = max(1f, tp.measureText(" "))
        inkT = ly0[0] - capH
        inkB = ly0[0]
    }

    val m = capH * t.pad / 100f
    val w = max(1f, (inkR - inkL) + 2 * m)
    val h = max(1f, (inkB - inkT) + 2 * m)
    val sx = m - inkL
    val sy = m - inkT
    val lx = FloatArray(lines.size) { lx0[it] + sx }
    val ly = FloatArray(lines.size) { ly0[it] + sy }

    val rad = t.corner / 100f * min(w, h) / 2f
    val a = min(rad, min(w, h) / 2f) * 0.29289322f
    val margin = t.fs * 0.5f
    val wp = Warp(w, h, t.curve)
    if (!wp.on) {
        return TextGeom(
            w, h, rad, lines, lx, ly, tp, 0f, 0f, w, h, null,
            Pt(a, a), Pt(w - a, h - a), Pt(a, h - a), margin
        )
    }
    val pts = outlinePoints(w, h, rad, 96).map { wp.map(it.x, it.y) }
    var x0 = Float.MAX_VALUE
    var y0 = Float.MAX_VALUE
    var x1 = -Float.MAX_VALUE
    var y1 = -Float.MAX_VALUE
    val path = Path()
    pts.forEachIndexed { i, p ->
        x0 = min(x0, p.x); y0 = min(y0, p.y)
        x1 = max(x1, p.x); y1 = max(y1, p.y)
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    path.close()
    return TextGeom(
        w, h, rad, lines, lx, ly, tp, x0, y0, x1, y1, path,
        wp.map(a, a), wp.map(w - a, h - a), wp.map(a, h - a), margin
    )
}

private fun drawGlyphs(c: Canvas, t: TxtItem, g: TextGeom) {
    val tp = g.paint
    if (t.curve == 0) {
        for (i in g.lines.indices) c.drawText(g.lines[i], g.lx[i], g.ly[i], tp)
        return
    }
    val gp = TextPaint(tp)
    gp.letterSpacing = 0f
    val wp = Warp(g.w, g.h, t.curve)
    val sgn = if (t.curve > 0) 1f else -1f
    for (li in g.lines.indices) {
        val line = g.lines[li]
        var i = 0
        while (i < line.length) {
            val cp = Character.codePointAt(line, i)
            val n = Character.charCount(cp)
            val next = i + n
            if (cp > 32) {
                val x0 = tp.measureText(line, 0, i)
                val x1 = tp.measureText(line, 0, next)
                val s = line.substring(i, next)
                val wd = gp.measureText(s)
                val u = g.lx[li] + (x0 + x1) / 2f
                val p = wp.map(u, g.ly[li])
                c.save()
                c.translate(p.x, p.y)
                c.rotate(sgn * wp.phi(u) * 57.29578f)
                c.drawText(s, -wd / 2f, 0f, gp)
                c.restore()
            }
            i = next
        }
    }
}

/** Zeichnet ein Textfeld. Ursprung (0, 0) = linke obere Ecke des unverbogenen Feldes. */
fun drawTextItem(c: Canvas, t: TxtItem, g: TextGeom) {
    val alpha = ((100 - t.transp) * 255 / 100).coerceIn(0, 255)
    if (alpha <= 0) return
    val m = g.margin
    val l = g.bx0 - m
    val top = g.by0 - m
    val r = g.bx1 + m
    val b = g.by1 + m
    val outer = if (alpha < 255) c.saveLayerAlpha(l, top, r, b, alpha) else c.save()

    val bg = t.bg
    if (bg != null) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        applyFill(p, bg, g.w, g.h)
        val path = g.path
        if (path == null) c.drawRoundRect(0f, 0f, g.w, g.h, g.rad, g.rad, p)
        else c.drawPath(path, p)
    }

    val gk = glitterKind(t.color)
    if (gk != 0) {
        val inner = c.saveLayer(l, top, r, b, null)
        drawGlyphs(c, t, g)
        val gp = Paint()
        gp.isFilterBitmap = true
        gp.shader = glitterShader(gk, g.w, g.h)
        gp.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        c.drawRect(l, top, r, b, gp)
        c.restoreToCount(inner)
    } else {
        drawGlyphs(c, t, g)
    }
    c.restoreToCount(outer)
}

/* ---------- Rahmen, Treffer und Griffe ---------- */

/**
 * Beschreibt ein Element (Bild oder Text) mit Mittelpunkt, Größe und Drehung.
 * Punkte (tl, br, bl, hx0..) liegen im Koordinatensystem des unverdrehten Feldes.
 */
class Frame(
    val cx: Float, val cy: Float, val w: Float, val h: Float, val rot: Float,
    val tl: Pt, val br: Pt, val bl: Pt,
    val hx0: Float, val hy0: Float, val hx1: Float, val hy1: Float,
    val rad: Float, val path: Path?
) {
    fun toUnit(q: Pt): Pt {
        val a = Math.toRadians(rot.toDouble())
        val cs = cos(a).toFloat()
        val sn = sin(a).toFloat()
        val dx = q.x - w / 2f
        val dy = q.y - h / 2f
        return Pt(cx + dx * cs - dy * sn, cy + dx * sn + dy * cs)
    }

    fun toLocal(u: Pt): Pt {
        val a = Math.toRadians(-rot.toDouble())
        val cs = cos(a).toFloat()
        val sn = sin(a).toFloat()
        val dx = u.x - cx
        val dy = u.y - cy
        return Pt(dx * cs - dy * sn + w / 2f, dx * sn + dy * cs + h / 2f)
    }

    fun contains(u: Pt, slop: Float): Boolean {
        val q = toLocal(u)
        return q.x >= hx0 - slop && q.x <= hx1 + slop && q.y >= hy0 - slop && q.y <= hy1 + slop
    }
}

fun imageFrame(s: EditorState, rects: List<Cell>, i: Int): Frame? {
    val r = rects.getOrNull(i) ?: return null
    val img = s.images.getOrNull(i) ?: return null
    val rad = s.rundung / 100f * min(r.w, r.h) / 2f
    val a = min(rad, min(r.w, r.h) / 2f) * 0.29289322f
    val rot = if (s.mode == Mode.FREISTIL) img.rot else 0f
    return Frame(
        r.x + r.w / 2f, r.y + r.h / 2f, r.w, r.h, rot,
        Pt(a, a), Pt(r.w - a, r.h - a), Pt(a, r.h - a),
        0f, 0f, r.w, r.h, rad, null
    )
}

fun frameOf(s: EditorState, item: Any): Frame? {
    if (item is ImgItem) {
        val i = s.images.indexOf(item)
        if (i < 0) return null
        return imageFrame(s, rectsOf(s), i)
    }
    if (item is TxtItem) {
        val g = textGeom(item)
        return Frame(
            item.x + g.w / 2f, item.y + g.h / 2f, g.w, g.h, item.rot,
            g.tl, g.br, g.bl, g.bx0, g.by0, g.bx1, g.by1, g.rad, g.path
        )
    }
    return null
}

/** Oberstes Element an der Stelle u (in Canvas-Einheiten). Texte liegen über Bildern. */
fun hitItem(s: EditorState, u: Pt, slop: Float): Any? {
    for (t in s.texts.asReversed()) {
        val f = frameOf(s, t) ?: continue
        if (f.contains(u, slop)) return t
    }
    val rects = rectsOf(s)
    for (i in s.images.indices.reversed()) {
        val f = imageFrame(s, rects, i) ?: continue
        if (f.contains(u, slop)) return s.images[i]
    }
    return null
}

/* ---------- Collage-Raster ---------- */

class Cell(val x: Float, val y: Float, val w: Float, val h: Float)

class CollageOpt(val rows: List<Int>, val transposed: Boolean)

fun collageOptions(n: Int): List<CollageOpt> {
    if (n <= 0) return emptyList()
    val set = LinkedHashSet<List<Int>>()
    val minR = ceil(n / 4.0).toInt()
    for (r in minR..min(n, minR + 2)) {
        val base = n / r
        val ex = n % r
        set.add(List(r) { if (it < ex) base + 1 else base })
        set.add(List(r) { if (it >= r - ex) base + 1 else base })
        val start = (r - ex) / 2
        set.add(List(r) { if (it >= start && it < start + ex) base + 1 else base })
        if (r >= 2) {
            val rr = r - 1
            val rest = n - 1
            val b = rest / rr
            val e = rest % rr
            val even = List(rr) { if (it < e) b + 1 else b }
            set.add(listOf(1) + even)
            set.add(even + listOf(1))
        }
    }
    val out = ArrayList<CollageOpt>()
    for (rows in set) {
        if (rows.any { it < 1 }) continue
        out.add(CollageOpt(rows, false))
        if (rows.toSet().size > 1) out.add(CollageOpt(rows, true))
    }
    return out.take(16)
}

fun collageCells(o: CollageOpt, W: Float, H: Float, gapIn: Float): List<Cell> {
    // Normal: Zeilen stapeln sich von oben nach unten.
    // Transponiert: "Zeilen" sind Spalten, die sich von links nach rechts stapeln.
    val mainLen = if (o.transposed) W else H
    val crossLen = if (o.transposed) H else W
    val rows = o.rows.size
    val maxK = max(rows, o.rows.maxOrNull() ?: 1)
    val gap = min(gapIn, min(W, H) * 0.6f / max(1, maxK - 1))
    val rh = (mainLen - (rows - 1) * gap) / rows
    val out = ArrayList<Cell>()
    for ((i, k) in o.rows.withIndex()) {
        val cw = (crossLen - (k - 1) * gap) / k
        for (j in 0 until k) {
            val a = j * (cw + gap)
            val b = i * (rh + gap)
            out.add(if (o.transposed) Cell(b, a, rh, cw) else Cell(a, b, cw, rh))
        }
    }
    return out
}

fun canvasSize(s: EditorState): Pair<Float, Float> {
    val imgs = s.images
    return when (s.mode) {
        Mode.FREISTIL, Mode.COLLAGE -> {
            val r = RATIOS[s.ratioIdx.coerceIn(0, RATIOS.size - 1)]
            if (r.w >= r.h) Pair(BASE, BASE * r.h / r.w) else Pair(BASE * r.w / r.h, BASE)
        }
        Mode.REIHE -> {
            if (imgs.isEmpty()) {
                Pair(BASE, BASE)
            } else {
                val g = s.gap.toFloat() * (imgs.size - 1)
                if (s.vertical) {
                    Pair(BASE, imgs.map { BASE * it.bmp.height / it.bmp.width }.sum() + g)
                } else {
                    Pair(imgs.map { BASE * it.bmp.width / it.bmp.height }.sum() + g, BASE)
                }
            }
        }
    }
}

fun rectsOf(s: EditorState): List<Cell> {
    val imgs = s.images
    val g = s.gap.toFloat()
    return when (s.mode) {
        Mode.FREISTIL -> imgs.map { Cell(it.x, it.y, it.w, it.h) }
        Mode.COLLAGE -> {
            val (cw, ch) = canvasSize(s)
            val opts = collageOptions(imgs.size)
            if (opts.isEmpty()) emptyList()
            else collageCells(opts[s.collageIdx.coerceIn(0, opts.size - 1)], cw, ch, g)
        }
        Mode.REIHE -> {
            var p = 0f
            if (s.vertical) {
                imgs.map {
                    val h = BASE * it.bmp.height / it.bmp.width
                    val c = Cell(0f, p, BASE, h)
                    p += h + g
                    c
                }
            } else {
                imgs.map {
                    val w = BASE * it.bmp.width / it.bmp.height
                    val c = Cell(p, 0f, w, BASE)
                    p += w + g
                    c
                }
            }
        }
    }
}

/* ---------- Bilder laden ---------- */

// Kleine Vorschau-Bilder für die Bearbeitung (maximal 1800 px)
suspend fun loadBitmaps(context: Context, uris: List<Uri>): List<Loaded> =
    withContext(Dispatchers.IO) {
        uris.mapNotNull { uri ->
            try {
                val src = ImageDecoder.createSource(context.contentResolver, uri)
                val bmp = ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
                    val m = max(info.size.width, info.size.height)
                    if (m > 1800) decoder.setTargetSampleSize(ceil(m / 1800.0).toInt())
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
                Loaded(bmp, uri)
            } catch (e: Throwable) {
                null
            }
        }
    }

// Original in hoher Auflösung für den Export (nur so groß wie nötig)
fun decodeForExport(context: Context, uri: Uri?, needW: Float, needH: Float): Bitmap? {
    if (uri == null) return null
    return try {
        val src = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
            val w0 = info.size.width.toFloat()
            val h0 = info.size.height.toFloat()
            var s = max(needW / w0, needH / h0)
            s = min(s, 1f)
            s = min(s, 6000f / max(w0, h0))
            if (s < 0.999f) {
                decoder.setTargetSize(
                    max(1, (w0 * s).roundToInt()),
                    max(1, (h0 * s).roundToInt())
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } catch (e: Throwable) {
        null
    }
}

/* ---------- Gemeinsame Zeichenroutine (Vorschau und Export) ---------- */

/**
 * Zeichnet die komplette Fläche in Canvas-Einheiten (0..cw, 0..ch).
 * Vorschau und Export benutzen genau diese Funktion, damit sie gleich aussehen.
 */
fun drawScene(
    c: Canvas,
    s: EditorState,
    fetch: (ImgItem, Cell) -> Bitmap,
    release: (ImgItem, Bitmap) -> Unit
) {
    val (cw, ch) = canvasSize(s)
    val rects = rectsOf(s)
    val free = s.mode == Mode.FREISTIL

    val bgp = Paint()
    if (free) bgp.color = android.graphics.Color.WHITE
    else applyFill(bgp, s.frameColor, cw, ch)
    c.drawRect(0f, 0f, cw, ch, bgp)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    s.images.forEachIndexed { i, img ->
        val r = rects.getOrNull(i)
        if (r != null) {
            val bmp = fetch(img, r)
            val bw = bmp.width.toFloat()
            val bh = bmp.height.toFloat()
            val sc = max(r.w / bw, r.h / bh)
            val cropW = r.w / sc
            val cropH = r.h / sc
            val left = ((bw - cropW) / 2f).roundToInt().coerceIn(0, bmp.width - 1)
            val top = ((bh - cropH) / 2f).roundToInt().coerceIn(0, bmp.height - 1)
            val right = (left + cropW).roundToInt().coerceIn(left + 1, bmp.width)
            val bottom = (top + cropH).roundToInt().coerceIn(top + 1, bmp.height)
            val src = Rect(left, top, right, bottom)

            val rot = if (free) img.rot else 0f
            c.save()
            c.translate(r.x + r.w / 2f, r.y + r.h / 2f)
            c.rotate(rot)
            c.translate(-r.w / 2f, -r.h / 2f)

            val dst = RectF(0f, 0f, r.w, r.h)
            val rad = s.rundung / 100f * min(r.w, r.h) / 2f
            if (rad > 0.5f) {
                // Bild in eigener Ebene zeichnen, dann alles AUSSERHALB der
                // Rundform wegradieren (Gegenteil der Rundform mit DST_OUT)
                val layer = c.saveLayer(dst, null)
                c.drawBitmap(bmp, src, dst, paint)
                val outside = Path()
                outside.addRoundRect(dst, rad, rad, Path.Direction.CW)
                outside.fillType = Path.FillType.INVERSE_WINDING
                val mp = Paint(Paint.ANTI_ALIAS_FLAG)
                mp.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                mp.color = android.graphics.Color.BLACK
                c.drawPath(outside, mp)
                c.restoreToCount(layer)
            } else {
                c.drawBitmap(bmp, src, dst, paint)
            }
            if (free && s.gap > 0) {
                val b = s.gap * 0.6f
                val bp = Paint(Paint.ANTI_ALIAS_FLAG)
                bp.style = Paint.Style.STROKE
                bp.strokeWidth = b
                applyFill(bp, s.frameColor, cw, ch)
                val inner = RectF(b / 2f, b / 2f, r.w - b / 2f, r.h - b / 2f)
                val ir = max(0f, rad - b / 2f)
                c.drawRoundRect(inner, ir, ir, bp)
            }
            c.restore()
            release(img, bmp)
        }
    }

    for (t in s.texts) {
        val g = textGeom(t)
        c.save()
        c.translate(t.x + g.w / 2f, t.y + g.h / 2f)
        c.rotate(t.rot)
        c.translate(-g.w / 2f, -g.h / 2f)
        drawTextItem(c, t, g)
        c.restore()
    }
}

/* ---------- Export ---------- */

fun renderBitmap(context: Context, s: EditorState): Bitmap {
    val (cw, ch) = canvasSize(s)
    var k = QUALITY_PX[s.quality.coerceIn(0, 2)] / BASE
    k = min(k, 16000f / max(cw, ch))
    k = min(k, sqrt(28_000_000f / (cw * ch)))
    val bw = max(1, (cw * k).roundToInt())
    val bh = max(1, (ch * k).roundToInt())
    val bmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    c.scale(k, k)
    val kk = k
    drawScene(
        c, s,
        fetch = { img, r ->
            val pw = img.bmp.width.toFloat()
            val ph = img.bmp.height.toFloat()
            val sc = max(r.w / pw, r.h / ph)
            val fx = min(1f, r.w / sc / pw)
            val fy = min(1f, r.h / sc / ph)
            decodeForExport(context, img.uri, r.w * kk / fx, r.h * kk / fy) ?: img.bmp
        },
        release = { img, b -> if (b !== img.bmp) b.recycle() }
    )
    return bmp
}

fun saveBitmap(context: Context, bmp: Bitmap, png: Boolean): Boolean {
    val resolver = context.contentResolver
    val ext = if (png) "png" else "jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "Bildgestalter_${System.currentTimeMillis()}.$ext")
        put(MediaStore.Images.Media.MIME_TYPE, if (png) "image/png" else "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Bildgestalter")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
    return try {
        var ok = false
        resolver.openOutputStream(uri)?.use { out ->
            ok = bmp.compress(
                if (png) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                95,
                out
            )
        }
        if (!ok) throw RuntimeException("Speichern fehlgeschlagen")
        val done = ContentValues()
        done.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, done, null, null)
        true
    } catch (e: Throwable) {
        resolver.delete(uri, null, null)
        false
    }
}

suspend fun exportImage(context: Context, s: EditorState): String? =
    withContext(Dispatchers.IO) {
        try {
            val bmp = renderBitmap(context, s)
            val res = if (saveBitmap(context, bmp, true)) "PNG"
            else if (saveBitmap(context, bmp, false)) "JPG"
            else null
            bmp.recycle()
            res
        } catch (e: Throwable) {
            null
        }
    }
