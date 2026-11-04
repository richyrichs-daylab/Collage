package de.bildgestalter

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
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
}

class TxtItem {
    var text by mutableStateOf("Text")
    var x by mutableStateOf(100f)
    var y by mutableStateOf(100f)
    var w by mutableStateOf(540f)
    var fs by mutableStateOf(64f)
    var font by mutableStateOf(0)
    var color by mutableStateOf(Color.Black)
    var bg by mutableStateOf<Color?>(null)
    var align by mutableStateOf(1)
}

class EditorState(val mode: Mode) {
    val images = mutableStateListOf<ImgItem>()
    val texts = mutableStateListOf<TxtItem>()
    var selected by mutableStateOf<Any?>(null)
    var vertical by mutableStateOf(true)
    var collageIdx by mutableStateOf(0)
    var gridOpen by mutableStateOf(false)

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

fun EditorState.addText(x: Float, y: Float, w: Float = 540f) {
    val t = TxtItem()
    t.x = x
    t.y = y
    t.w = w
    t.fs = 64f * w / 540f
    texts.add(t)
    selected = t
    gridOpen = false
    frameOpen = false
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

/* ---------- Export ---------- */

fun renderBitmap(context: Context, s: EditorState): Bitmap {
    val (cw, ch) = canvasSize(s)
    val rects = rectsOf(s)
    var k = QUALITY_PX[s.quality.coerceIn(0, 2)] / BASE
    k = min(k, 16000f / max(cw, ch))
    k = min(k, sqrt(28_000_000f / (cw * ch)))
    val bw = max(1, (cw * k).roundToInt())
    val bh = max(1, (ch * k).roundToInt())
    val bmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val free = s.mode == Mode.FREISTIL
    c.drawColor(if (free) android.graphics.Color.WHITE else s.frameColor.toArgb())
    c.scale(k, k)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    s.images.forEachIndexed { i, img ->
        val r = rects.getOrNull(i)
        if (r != null) {
            val pw = img.bmp.width.toFloat()
            val ph = img.bmp.height.toFloat()
            val sc = max(r.w / pw, r.h / ph)
            val fx = min(1f, r.w / sc / pw)
            val fy = min(1f, r.h / sc / ph)

            val full = decodeForExport(context, img.uri, r.w * k / fx, r.h * k / fy)
            val srcBmp = full ?: img.bmp
            val sw0 = srcBmp.width
            val sh0 = srcBmp.height
            val cropW = sw0 * fx
            val cropH = sh0 * fy
            val left = ((sw0 - cropW) / 2f).roundToInt().coerceIn(0, sw0 - 1)
            val top = ((sh0 - cropH) / 2f).roundToInt().coerceIn(0, sh0 - 1)
            val right = (left + cropW).roundToInt().coerceIn(left + 1, sw0)
            val bottom = (top + cropH).roundToInt().coerceIn(top + 1, sh0)
            val src = Rect(left, top, right, bottom)

            val dst = RectF(r.x, r.y, r.x + r.w, r.y + r.h)
            val rad = s.rundung / 100f * min(r.w, r.h) / 2f
            if (rad > 0.5f) {
                // Bild in eigener Ebene zeichnen, dann alles AUSSERHALB der
                // Rundform wegradieren (Gegenteil der Rundform mit DST_OUT)
                val layer = c.saveLayer(dst, null)
                c.drawBitmap(srcBmp, src, dst, paint)
                val outside = Path()
                outside.addRoundRect(dst, rad, rad, Path.Direction.CW)
                outside.fillType = Path.FillType.INVERSE_WINDING
                val mp = Paint(Paint.ANTI_ALIAS_FLAG)
                mp.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                mp.color = android.graphics.Color.BLACK
                c.drawPath(outside, mp)
                c.restoreToCount(layer)
            } else {
                c.drawBitmap(srcBmp, src, dst, paint)
            }
            if (free && s.gap > 0) {
                val b = s.gap * 0.6f
                val bp = Paint(Paint.ANTI_ALIAS_FLAG)
                bp.style = Paint.Style.STROKE
                bp.strokeWidth = b
                bp.color = s.frameColor.toArgb()
                val inner = RectF(dst.left + b / 2f, dst.top + b / 2f, dst.right - b / 2f, dst.bottom - b / 2f)
                val ir = max(0f, rad - b / 2f)
                c.drawRoundRect(inner, ir, ir, bp)
            }
            if (full != null) full.recycle()
        }
    }

    for (t in s.texts) {
        val pad = t.fs * 0.25f
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG)
        tp.textSize = t.fs
        tp.color = t.color.toArgb()
        tp.typeface = typefaceOf(t.font)
        val inner = max(1, (t.w - 2 * pad).roundToInt())
        val txt = if (t.text.isEmpty()) " " else t.text
        val al = when (t.align) {
            0 -> Layout.Alignment.ALIGN_NORMAL
            2 -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }
        val sl = StaticLayout.Builder.obtain(txt, 0, txt.length, tp, inner)
            .setAlignment(al)
            .setIncludePad(false)
            .setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE)
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
            .build()
        val bgc = t.bg
        if (bgc != null) {
            val bp = Paint()
            bp.color = bgc.toArgb()
            c.drawRect(t.x, t.y, t.x + t.w, t.y + sl.height + 2 * pad, bp)
        }
        c.save()
        c.translate(t.x + pad, t.y + pad)
        sl.draw(c)
        c.restore()
    }
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
