package de.bildgestalter

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
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

val FONT_NAMES = listOf("Standard", "Serif", "Schreibmaschine", "Handschrift", "Fett", "Kursiv")

fun typefaceOf(i: Int): Typeface = when (i) {
    0 -> Typeface.SANS_SERIF
    1 -> Typeface.SERIF
    2 -> Typeface.MONOSPACE
    3 -> Typeface.create("cursive", Typeface.NORMAL)
    4 -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    else -> Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
}

class ImgItem(val bmp: Bitmap) {
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
}

fun EditorState.addImages(bmps: List<Bitmap>) {
    for (b in bmps) {
        val item = ImgItem(b)
        if (mode == Mode.FREISTIL) {
            var w = 540f
            var h = w * b.height / b.width
            if (h > 900f) {
                w *= 900f / h
                h = 900f
            }
            val off = (images.size % 6) * 40f
            item.w = w
            item.h = h
            item.x = 100f + off
            item.y = 120f + off
        }
        images.add(item)
    }
}

fun EditorState.addText(x: Float, y: Float) {
    val t = TxtItem()
    t.x = x
    t.y = y
    texts.add(t)
    selected = t
    gridOpen = false
}

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

fun collageCells(o: CollageOpt, size: Float, gap: Float): List<Cell> {
    val rows = o.rows.size
    val rh = (size - (rows - 1) * gap) / rows
    val out = ArrayList<Cell>()
    for ((i, k) in o.rows.withIndex()) {
        val cw = (size - (k - 1) * gap) / k
        for (j in 0 until k) {
            val x = j * (cw + gap)
            val y = i * (rh + gap)
            out.add(if (o.transposed) Cell(y, x, rh, cw) else Cell(x, y, cw, rh))
        }
    }
    return out
}

fun canvasSize(s: EditorState): Pair<Float, Float> {
    val imgs = s.images
    return when (s.mode) {
        Mode.FREISTIL -> Pair(BASE, 1350f)
        Mode.COLLAGE -> Pair(BASE, BASE)
        Mode.REIHE -> {
            if (imgs.isEmpty()) {
                Pair(BASE, BASE)
            } else if (s.vertical) {
                Pair(BASE, imgs.map { BASE * it.bmp.height / it.bmp.width }.sum())
            } else {
                Pair(imgs.map { BASE * it.bmp.width / it.bmp.height }.sum(), BASE)
            }
        }
    }
}

fun rectsOf(s: EditorState): List<Cell> {
    val imgs = s.images
    return when (s.mode) {
        Mode.FREISTIL -> imgs.map { Cell(it.x, it.y, it.w, it.h) }
        Mode.COLLAGE -> {
            val opts = collageOptions(imgs.size)
            if (opts.isEmpty()) emptyList()
            else collageCells(opts[s.collageIdx.coerceIn(0, opts.size - 1)], BASE, 8f)
        }
        Mode.REIHE -> {
            var p = 0f
            if (s.vertical) {
                imgs.map {
                    val h = BASE * it.bmp.height / it.bmp.width
                    val c = Cell(0f, p, BASE, h)
                    p += h
                    c
                }
            } else {
                imgs.map {
                    val w = BASE * it.bmp.width / it.bmp.height
                    val c = Cell(p, 0f, w, BASE)
                    p += w
                    c
                }
            }
        }
    }
}

suspend fun loadBitmaps(context: Context, uris: List<Uri>): List<Bitmap> =
    withContext(Dispatchers.IO) {
        uris.mapNotNull { uri ->
            try {
                val src = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
                    val m = max(info.size.width, info.size.height)
                    if (m > 1800) decoder.setTargetSampleSize(ceil(m / 1800.0).toInt())
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } catch (e: Throwable) {
                null
            }
        }
    }

fun renderBitmap(s: EditorState): Bitmap {
    val (cw, ch) = canvasSize(s)
    val rects = rectsOf(s)
    var k = 1f
    k = min(k, 16000f / max(cw, ch))
    k = min(k, sqrt(27_500_000f / (cw * ch)))
    val bw = max(1, (cw * k).roundToInt())
    val bh = max(1, (ch * k).roundToInt())
    val bmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    c.drawColor(android.graphics.Color.WHITE)
    c.scale(k, k)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    s.images.forEachIndexed { i, img ->
        val r = rects.getOrNull(i)
        if (r != null) {
            val iw = img.bmp.width.toFloat()
            val ih = img.bmp.height.toFloat()
            val sc = max(r.w / iw, r.h / ih)
            val sw = r.w / sc
            val sh = r.h / sc
            val src = Rect(
                ((iw - sw) / 2f).roundToInt().coerceAtLeast(0),
                ((ih - sh) / 2f).roundToInt().coerceAtLeast(0),
                ((iw + sw) / 2f).roundToInt().coerceAtMost(img.bmp.width),
                ((ih + sh) / 2f).roundToInt().coerceAtMost(img.bmp.height)
            )
            c.drawBitmap(img.bmp, src, RectF(r.x, r.y, r.x + r.w, r.y + r.h), paint)
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
            val bmp = renderBitmap(s)
            if (saveBitmap(context, bmp, true)) "PNG"
            else if (saveBitmap(context, bmp, false)) "JPG"
            else null
        } catch (e: Throwable) {
            null
        }
    }
