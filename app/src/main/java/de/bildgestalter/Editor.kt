package de.bildgestalter

import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val Teal = Color(0xFF3AA8C1)
private val Grau = Color(0xFFA8A8A8)
private val DimColor = Color(0xFFC8C8C8).copy(alpha = 0.5f)

// Zusätzlicher Rand um jedes Element für die Eckkreise (x und Pfeil)
private val ITEM_PAD = 22.dp

private val PALETTE = listOf(
    Color.Black, Color.White, Color(0xFFE53935), Color(0xFFFB8C00),
    Color(0xFFFDD835), Color(0xFF43A047), Color(0xFFB5ECF2), Color(0xFF1E88E5),
    Color(0xFF8E24AA), Color(0xFFEC407A), Color(0xFF757575), Color(0xFF6D4C41),
    GOLD, SILVER
)

/** Brush für eine Vorschau-Fläche (Glitzer oder einfarbig). */
private fun fillBrush(c: Color, w: Float, h: Float, k: Float): Brush {
    val gk = glitterKind(c)
    return if (gk == 0) SolidColor(c) else ShaderBrush(glitterShader(gk, w, h, k))
}

/** Kleiner Verlauf für Farbfelder (Punkte und Icons) in den Einstellungen. */
private fun swatchBrush(c: Color): Brush = when (glitterKind(c)) {
    1 -> Brush.linearGradient(
        listOf(Color(0xFFB8860B), Color(0xFFFFE27A), Color(0xFFD4AF37), Color(0xFFFFF3B0), Color(0xFFC9971C))
    )
    2 -> Brush.linearGradient(
        listOf(Color(0xFF8E8E8E), Color(0xFFF5F5F5), Color(0xFFBDBDBD), Color(0xFFFFFFFF), Color(0xFFA0A0A0))
    )
    else -> SolidColor(c)
}

/** Nebelig ausblendende Ränder für horizontal scrollbare Zeilen. */
private fun Modifier.fadingEdges(scroll: ScrollState, fade: Dp = 28.dp): Modifier =
    this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val f = fade.toPx()
            val left = min(scroll.value.toFloat(), f)
            val right = min((scroll.maxValue - scroll.value).toFloat(), f)
            if (left > 0.5f) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 1f - left / f), Color.Black),
                        startX = 0f,
                        endX = f
                    ),
                    topLeft = Offset.Zero,
                    size = Size(f, size.height),
                    blendMode = BlendMode.DstIn
                )
            }
            if (right > 0.5f) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.Black.copy(alpha = 1f - right / f)),
                        startX = size.width - f,
                        endX = size.width
                    ),
                    topLeft = Offset(size.width - f, 0f),
                    size = Size(f, size.height),
                    blendMode = BlendMode.DstIn
                )
            }
        }

@Composable
fun EditorScreen(state: EditorState, onBack: () -> Unit) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val padPx = with(density) { ITEM_PAD.roundToPx() }
    val scope = rememberCoroutineScope()
    var showRes by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch { state.addImages(loadBitmaps(context, uris)) }
        }
    }
    val pick: () -> Unit = {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()
    val scaleRef = remember { floatArrayOf(1f) }

    val addText: () -> Unit = {
        val (cw, ch) = canvasSize(state)
        val tw = min(540f, cw * 0.8f)
        var x = (cw - tw) / 2f
        var y = ch * 0.4f
        if (state.mode == Mode.REIHE) {
            val sc = scaleRef[0]
            if (state.vertical) {
                y = vScroll.value / sc + 120f
            } else {
                x = hScroll.value / sc + 120f
            }
        }
        state.addText(x, y, tw)
    }

    val export: () -> Unit = {
        if (!exporting) {
            if (state.images.isEmpty() && state.texts.isEmpty()) {
                Toast.makeText(context, "Bitte zuerst Bilder oder Text hinzufügen.", Toast.LENGTH_SHORT).show()
            } else {
                exporting = true
                state.selected = null
                Toast.makeText(context, "Wird exportiert …", Toast.LENGTH_SHORT).show()
                scope.launch {
                    val r = exportImage(context, state)
                    exporting = false
                    val msg = if (r != null) "Gespeichert als $r in der Galerie (Ordner Bildgestalter)"
                    else "Export fehlgeschlagen. Versuche eine niedrigere Bildauflösung."
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val toggleFrame: () -> Unit = {
        state.frameOpen = !state.frameOpen
        if (state.frameOpen) {
            state.gridOpen = false
            state.selected = null
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFE6E6E6))
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .height(56.dp)
                .padding(horizontal = 8.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Text(
                "←", fontSize = 30.sp, color = Color.Black,
                modifier = Modifier.clickable { onBack() }.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Bildauflösung", fontSize = 15.sp, color = Color.Black,
                    modifier = Modifier.clickable { showRes = true }.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                Text(
                    "Exportieren", fontSize = 15.sp, color = Color.Black,
                    modifier = Modifier.clickable { export() }.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }

        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .background(Color(0xFFC8C8C8))
        ) {
            val awI = constraints.maxWidth
            val ahI = constraints.maxHeight
            val aw = awI.toFloat()
            val ah = ahI.toFloat()
            val (cw, ch) = canvasSize(state)
            val p2 = 2f * padPx + 4f
            val scale = when {
                state.mode != Mode.REIHE -> min((aw - p2) / cw, (ah - p2) / ch)
                state.vertical -> min(aw * 0.84f, aw - p2) / cw
                else -> min(ah * 0.84f, ah - p2) / ch
            }
            scaleRef[0] = scale
            val sw = (cw * scale).roundToInt()
            val sh = (ch * scale).roundToInt()

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            state.selected = null
                            state.gridOpen = false
                        })
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.mode != Mode.REIHE) {
                    Stage(state, cw, ch, scale, (awI - sw) / 2, (ahI - sh) / 2, awI, ahI)
                } else if (state.vertical) {
                    Column(
                        Modifier.fillMaxWidth().verticalScroll(vScroll),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Stage(state, cw, ch, scale, padPx, padPx, sw + 2 * padPx, sh + 2 * padPx)
                        Spacer(Modifier.height(16.dp))
                    }
                } else {
                    Row(
                        Modifier.fillMaxHeight().horizontalScroll(hScroll),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(16.dp))
                        Stage(state, cw, ch, scale, padPx, padPx, sw + 2 * padPx, sh + 2 * padPx)
                        Spacer(Modifier.width(16.dp))
                    }
                }
            }
        }

        val sel = state.selected
        if (sel is TxtItem) TextPanel(sel)
        if (state.gridOpen && state.mode == Mode.COLLAGE) GridPicker(state)
        if (state.frameOpen) FramePanel(state)
        BottomBar(state, pick, addText, toggleFrame)
    }

    if (showRes) {
        ResolutionDialog(
            current = state.quality,
            onSelect = { i ->
                state.quality = i
                saveQuality(context, state.mode, i)
            },
            onDismiss = { showRes = false }
        )
    }
}

/**
 * ox, oy: Position des weißen Feldes im Container (in Pixeln).
 * boxW, boxH: Größe des Containers (in Pixeln).
 */
@Composable
fun Stage(
    state: EditorState, cw: Float, ch: Float, scale: Float,
    ox: Int, oy: Int, boxW: Int, boxH: Int
) {
    val density = LocalDensity.current
    val rects = rectsOf(state)
    val free = state.mode == Mode.FREISTIL
    val sw = (cw * scale).roundToInt()
    val sh = (ch * scale).roundToInt()
    val frameBrush = remember(state.frameColor, sw, sh, scale) {
        fillBrush(state.frameColor, sw.toFloat(), sh.toFloat(), scale)
    }
    val stageBrush: Brush = if (free) SolidColor(Color.White) else frameBrush

    Box(Modifier.size(with(density) { boxW.toDp() }, with(density) { boxH.toDp() })) {
        // Das Feld (leer bleibt es ohne Hinweistext)
        Box(
            Modifier
                .offset { IntOffset(ox, oy) }
                .size(with(density) { sw.toDp() }, with(density) { sh.toDp() })
                .background(stageBrush)
        )
        state.images.forEachIndexed { i, img ->
            val r = rects.getOrNull(i)
            if (r != null) {
                key(img) {
                    val bitmap = remember(img) { img.bmp.asImageBitmap() }
                    // Pixelgenaue Kanten: keine Lücken durch Rundung
                    val x0 = (r.x * scale).roundToInt()
                    val y0 = (r.y * scale).roundToInt()
                    val x1 = ((r.x + r.w) * scale).roundToInt()
                    val y1 = ((r.y + r.h) * scale).roundToInt()
                    val radPx = state.rundung / 100f * min(r.w, r.h) / 2f * scale
                    val shape = RoundedCornerShape(with(density) { radPx.toDp() })
                    val borderPx = state.gap * 0.6f * scale
                    ItemFrame(
                        x = (ox + x0).toFloat(), y = (oy + y0).toFloat(),
                        w = (x1 - x0).toFloat(), h = (y1 - y0).toFloat(),
                        selected = state.selected === img,
                        movable = free, resizable = free,
                        onSelect = { state.selected = img; state.gridOpen = false },
                        onMove = { dx, dy -> img.x += dx / scale; img.y += dy / scale },
                        onResize = { d ->
                            val nw = max(60f, img.w + d / scale)
                            val k = nw / img.w
                            img.w = nw
                            img.h = img.h * k
                        },
                        onDelete = {
                            state.images.remove(img)
                            if (state.selected === img) state.selected = null
                        }
                    ) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(shape)
                                .then(
                                    if (free && borderPx >= 1f)
                                        Modifier.border(with(density) { borderPx.toDp() }, frameBrush, shape)
                                    else Modifier
                                )
                                .then(
                                    if (free) Modifier.drawWithContent {
                                        drawContent()
                                        // Alles außerhalb des weißen Feldes: 50 % graue Überlagerung
                                        val l = -x0.toFloat()
                                        val tp = -y0.toFloat()
                                        clipRect(
                                            left = l, top = tp,
                                            right = l + sw, bottom = tp + sh,
                                            clipOp = ClipOp.Difference
                                        ) {
                                            drawRect(DimColor)
                                        }
                                    } else Modifier
                                )
                        )
                    }
                }
            }
        }
        state.texts.forEach { t ->
            key(t) {
                TextItemView(
                    t = t,
                    selected = state.selected === t,
                    ox = ox, oy = oy, scale = scale, sw = sw, sh = sh,
                    onSelect = { state.selected = t; state.gridOpen = false; state.frameOpen = false },
                    onMove = { dx, dy -> t.x += dx / scale; t.y += dy / scale },
                    onResize = { d ->
                        val nw = max(60f, t.w + d / scale)
                        val k = nw / t.w
                        t.w = nw
                        t.fs = t.fs * k
                    },
                    onDelete = {
                        state.texts.remove(t)
                        if (state.selected === t) state.selected = null
                    }
                )
            }
        }
    }
}

/* ---------- Eckkreise ---------- */

@Composable
fun DeleteHandle(modifier: Modifier, onDelete: () -> Unit) {
    val cur by rememberUpdatedState(onDelete)
    Box(
        modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFE53935))
            .pointerInput(Unit) { detectTapGestures(onTap = { cur() }) },
        contentAlignment = Alignment.Center
    ) {
        Text("×", color = Color.White, fontSize = 24.sp)
    }
}

@Composable
fun ResizeHandle(modifier: Modifier, onResize: (Float) -> Unit) {
    val cur by rememberUpdatedState(onResize)
    Box(
        modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0xFF999999), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    cur((drag.x + drag.y) / 2f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(18.dp)) {
            val s = size.minDimension
            val sw = 2.5.dp.toPx()
            val c = Color.Black
            drawLine(c, Offset(s * 0.15f, s * 0.15f), Offset(s * 0.85f, s * 0.85f), sw, StrokeCap.Round)
            drawLine(c, Offset(s * 0.85f, s * 0.85f), Offset(s * 0.45f, s * 0.85f), sw, StrokeCap.Round)
            drawLine(c, Offset(s * 0.85f, s * 0.85f), Offset(s * 0.85f, s * 0.45f), sw, StrokeCap.Round)
            drawLine(c, Offset(s * 0.15f, s * 0.15f), Offset(s * 0.55f, s * 0.15f), sw, StrokeCap.Round)
            drawLine(c, Offset(s * 0.15f, s * 0.15f), Offset(s * 0.15f, s * 0.55f), sw, StrokeCap.Round)
        }
    }
}

/**
 * x, y: Position der linken oberen Ecke des Inhalts im Container (Pixel).
 * w, h: Größe des Inhalts in Pixeln.
 * Der Rahmen um den Inhalt (ITEM_PAD ringsum) bietet Platz für die Eckkreise.
 */
@Composable
fun ItemFrame(
    x: Float, y: Float, w: Float, h: Float?,
    selected: Boolean, movable: Boolean, resizable: Boolean,
    onSelect: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onResize: (Float) -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val padPx = with(density) { ITEM_PAD.roundToPx() }
    val padDp = with(density) { padPx.toDp() }
    val curSelect by rememberUpdatedState(onSelect)
    val curMove by rememberUpdatedState(onMove)
    val totalW = max(0, w.roundToInt()) + 2 * padPx
    val totalH: Int? = h?.let { max(0, it.roundToInt()) + 2 * padPx }

    Box(
        Modifier
            .offset { IntOffset(x.roundToInt() - padPx, y.roundToInt() - padPx) }
            .layout { measurable, _ ->
                val c = if (totalH != null) {
                    Constraints.fixed(totalW, totalH)
                } else {
                    Constraints(
                        minWidth = totalW, maxWidth = totalW,
                        minHeight = 0, maxHeight = Constraints.Infinity
                    )
                }
                val p = measurable.measure(c)
                layout(p.width, p.height) { p.place(0, 0) }
            }
    ) {
        Box(
            Modifier
                .padding(padDp)
                .pointerInput(Unit) { detectTapGestures(onTap = { curSelect() }) }
                .pointerInput(movable) {
                    if (movable) {
                        detectDragGestures(
                            onDragStart = { curSelect() },
                            onDrag = { change, drag ->
                                change.consume()
                                curMove(drag.x, drag.y)
                            }
                        )
                    }
                }
                .then(if (selected) Modifier.border(2.dp, Teal) else Modifier)
        ) {
            content()
        }

        if (selected) {
            DeleteHandle(Modifier.align(Alignment.TopStart), onDelete)
            if (resizable) {
                ResizeHandle(Modifier.align(Alignment.BottomEnd), onResize)
            }
        }
    }
}

/**
 * Textfeld: wird mit derselben Routine gezeichnet wie im Export.
 * Auswahlrahmen und Eckkreise folgen Rundung und Krümmung.
 */
@Composable
fun TextItemView(
    t: TxtItem, selected: Boolean,
    ox: Int, oy: Int, scale: Float, sw: Int, sh: Int,
    onSelect: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onResize: (Float) -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    val padPx = with(density) { ITEM_PAD.roundToPx() }
    val halfHandle = with(density) { 18.dp.toPx() }
    val strokePx = with(density) { 2.dp.toPx() }
    val curSelect by rememberUpdatedState(onSelect)
    val curMove by rememberUpdatedState(onMove)
    val dimPaint = remember {
        android.graphics.Paint().apply {
            color = 0x80C8C8C8.toInt()
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        }
    }

    val g = textGeom(t)
    val left = ox + ((t.x + g.bx0) * scale).roundToInt()
    val top = oy + ((t.y + g.by0) * scale).roundToInt()
    val bw = max(1, ((g.bx1 - g.bx0) * scale).roundToInt())
    val bh = max(1, ((g.by1 - g.by0) * scale).roundToInt())

    Box(
        Modifier
            .offset { IntOffset(left - padPx, top - padPx) }
            .size(
                with(density) { (bw + 2 * padPx).toDp() },
                with(density) { (bh + 2 * padPx).toDp() }
            )
    ) {
        Canvas(
            Modifier
                .offset { IntOffset(padPx, padPx) }
                .size(with(density) { bw.toDp() }, with(density) { bh.toDp() })
                .pointerInput(Unit) { detectTapGestures(onTap = { curSelect() }) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { curSelect() },
                        onDrag = { change, drag ->
                            change.consume()
                            curMove(drag.x, drag.y)
                        }
                    )
                }
        ) {
            val nc = drawContext.canvas.nativeCanvas
            val m = g.margin * scale
            val layer = nc.saveLayer(-m, -m, bw + m, bh + m, null)

            nc.save()
            nc.scale(scale, scale)
            nc.translate(-g.bx0, -g.by0)
            drawTextItem(nc, t, g)
            nc.restore()

            // Außerhalb des weißen Feldes: 50 % graue Überlagerung (nur auf Text-Pixeln)
            val fl = (ox - left).toFloat()
            val ft = (oy - top).toFloat()
            nc.save()
            nc.clipOutRect(fl, ft, fl + sw, ft + sh)
            nc.drawRect(-m, -m, bw + m, bh + m, dimPaint)
            nc.restore()
            nc.restoreToCount(layer)

            if (selected) {
                val sp = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                sp.style = android.graphics.Paint.Style.STROKE
                sp.strokeWidth = strokePx / scale
                sp.color = Teal.toArgb()
                nc.save()
                nc.scale(scale, scale)
                nc.translate(-g.bx0, -g.by0)
                val path = g.path
                if (path == null) nc.drawRoundRect(0f, 0f, g.w, g.h, g.rad, g.rad, sp)
                else nc.drawPath(path, sp)
                nc.restore()
            }
        }

        if (selected) {
            val tlx = padPx + (g.tl.x - g.bx0) * scale
            val tly = padPx + (g.tl.y - g.by0) * scale
            val brx = padPx + (g.br.x - g.bx0) * scale
            val bry = padPx + (g.br.y - g.by0) * scale
            DeleteHandle(
                Modifier.offset { IntOffset((tlx - halfHandle).roundToInt(), (tly - halfHandle).roundToInt()) },
                onDelete
            )
            ResizeHandle(
                Modifier.offset { IntOffset((brx - halfHandle).roundToInt(), (bry - halfHandle).roundToInt()) },
                onResize
            )
        }
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .clip(shape)
            .background(if (selected) Color(0xFFB5ECF2) else Color.White)
            .border(1.dp, if (selected) Teal else Color(0xFFAAAAAA), shape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
fun ColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    val gk = glitterKind(color)
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(swatchBrush(color))
            .border(
                if (selected) 3.dp else 1.dp,
                if (selected) Teal else Color(0xFF888888),
                CircleShape
            )
            .clickable { onClick() }
    ) {
        if (gk != 0) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val wh = Color.White.copy(alpha = 0.95f)
                drawCircle(wh, 1.6.dp.toPx(), Offset(w * 0.28f, h * 0.32f))
                drawCircle(wh, 1.1.dp.toPx(), Offset(w * 0.68f, h * 0.62f))
                drawCircle(wh, 0.9.dp.toPx(), Offset(w * 0.45f, h * 0.78f))
                val c = Offset(w * 0.70f, h * 0.26f)
                val l = 3.5.dp.toPx()
                drawLine(wh, Offset(c.x - l, c.y), Offset(c.x + l, c.y), 1.2.dp.toPx(), StrokeCap.Round)
                drawLine(wh, Offset(c.x, c.y - l), Offset(c.x, c.y + l), 1.2.dp.toPx(), StrokeCap.Round)
            }
        }
    }
}

/* ---------- Icons und Slider ---------- */

@Composable
private fun ThicknessIcon(percent: Int) {
    Canvas(Modifier.size(28.dp)) {
        val sw = 2.dp.toPx()
        val c = Color(0xFF444444)
        val tw = (1.5f + percent / 100f * 6.5f).dp.toPx()
        val y = size.height * 0.4f
        val x = size.width / 2f
        // Trennlinien (T) werden mit der Dicke breiter
        drawRect(
            c,
            Offset(sw, max(sw, y - tw / 2f)),
            Size(size.width - 2 * sw, tw)
        )
        drawRect(
            c,
            Offset(x - tw / 2f, y),
            Size(tw, size.height - sw - y)
        )
        drawRoundRect(
            c, Offset(sw / 2f, sw / 2f),
            Size(size.width - sw, size.height - sw),
            CornerRadius(3.dp.toPx()), Stroke(sw)
        )
    }
}

@Composable
private fun RoundnessIcon(percent: Int) {
    Canvas(Modifier.size(width = 28.dp, height = 22.dp)) {
        val sw = 2.dp.toPx()
        val c = Color(0xFF444444)
        val r = percent / 100f * (min(size.width, size.height) - sw) / 2f
        drawRoundRect(
            c, Offset(sw / 2f, sw / 2f),
            Size(size.width - sw, size.height - sw),
            CornerRadius(r), Stroke(sw)
        )
    }
}

@Composable
private fun CornerIcon(percent: Int) {
    Canvas(Modifier.size(28.dp)) {
        val sw = 2.dp.toPx()
        val c = Color(0xFF444444)
        val m = sw
        val side = size.width - 2 * m
        val r = percent / 100f * side
        val path = Path()
        path.moveTo(m, size.height - m)
        path.lineTo(m, m + r)
        if (r > 0.5f) path.arcTo(Rect(m, m, m + 2 * r, m + 2 * r), 180f, 90f, false)
        path.lineTo(size.width - m, m)
        drawPath(path, c, style = Stroke(sw, cap = StrokeCap.Round))
    }
}

@Composable
private fun ColorSquareIcon(color: Color) {
    Box(
        Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(swatchBrush(color))
            .border(1.5.dp, Color(0xFF444444), RoundedCornerShape(4.dp))
    )
}

@Composable
private fun TransparencyIcon(percent: Int) {
    Box(
        Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF222222).copy(alpha = 1f - percent / 100f))
    )
}

@Composable
private fun DoubleArrowIcon(vertical: Boolean) {
    Canvas(Modifier.size(28.dp)) {
        val sw = 2.dp.toPx()
        val c = Color(0xFF444444)
        val a = 3.dp.toPx()
        val hd = 5.dp.toPx()
        if (vertical) {
            val x = size.width / 2f
            val e = size.height - a
            drawLine(c, Offset(x, a), Offset(x, e), sw, StrokeCap.Round)
            drawLine(c, Offset(x, a), Offset(x - hd, a + hd), sw, StrokeCap.Round)
            drawLine(c, Offset(x, a), Offset(x + hd, a + hd), sw, StrokeCap.Round)
            drawLine(c, Offset(x, e), Offset(x - hd, e - hd), sw, StrokeCap.Round)
            drawLine(c, Offset(x, e), Offset(x + hd, e - hd), sw, StrokeCap.Round)
        } else {
            val y = size.height / 2f
            val e = size.width - a
            drawLine(c, Offset(a, y), Offset(e, y), sw, StrokeCap.Round)
            drawLine(c, Offset(a, y), Offset(a + hd, y - hd), sw, StrokeCap.Round)
            drawLine(c, Offset(a, y), Offset(a + hd, y + hd), sw, StrokeCap.Round)
            drawLine(c, Offset(e, y), Offset(e - hd, y - hd), sw, StrokeCap.Round)
            drawLine(c, Offset(e, y), Offset(e - hd, y + hd), sw, StrokeCap.Round)
        }
    }
}

@Composable
private fun CurveIcon(percent: Int) {
    Canvas(Modifier.size(width = 32.dp, height = 24.dp)) {
        val wp = Warp(24f, 5f, percent)
        val pts = outlinePoints(24f, 5f, 0f, 64).map { wp.map(it.x, it.y) }
        var x0 = Float.MAX_VALUE
        var y0 = Float.MAX_VALUE
        var x1 = -Float.MAX_VALUE
        var y1 = -Float.MAX_VALUE
        for (p in pts) {
            x0 = min(x0, p.x); x1 = max(x1, p.x)
            y0 = min(y0, p.y); y1 = max(y1, p.y)
        }
        val sq = 0.75f   // zum Oval gestaucht
        val bw = max(0.01f, x1 - x0)
        val bh = max(0.01f, (y1 - y0) * sq)
        val s = min(size.width / bw, size.height / bh) * 0.9f
        val offX = (size.width - bw * s) / 2f
        val offY = (size.height - bh * s) / 2f
        val path = Path()
        pts.forEachIndexed { i, p ->
            val x = offX + (p.x - x0) * s
            val y = offY + (p.y - y0) * sq * s
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, Color(0xFF444444))
    }
}

@Composable
private fun FrameSlider(
    value: Int, onChange: (Int) -> Unit, modifier: Modifier,
    lo: Int = 0, hi: Int = 100
) {
    Slider(
        value = value.toFloat(),
        onValueChange = { onChange(it.roundToInt()) },
        valueRange = lo.toFloat()..hi.toFloat(),
        steps = hi - lo - 1,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Teal,
            activeTrackColor = Teal,
            inactiveTrackColor = Color(0xFFCCCCCC),
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent
        )
    )
}

@Composable
private fun SliderRow(
    icon: @Composable () -> Unit,
    value: Int, onChange: (Int) -> Unit,
    lo: Int = 0, hi: Int = 100
) {
    Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) { icon() }
        FrameSlider(value, onChange, Modifier.weight(1f), lo, hi)
    }
}

/* ---------- Text-Panel ---------- */

@Composable
fun TextPanel(t: TxtItem) {
    val s1 = rememberScrollState()
    val s2 = rememberScrollState()
    val s3 = rememberScrollState()
    val s4 = rememberScrollState()
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F4F4))
            .heightIn(max = 220.dp)
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = t.text,
            onValueChange = { t.text = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Text") },
            maxLines = 3
        )
        Text("Schriftart", fontSize = 12.sp, color = Color.Black)
        Row(
            Modifier.fillMaxWidth().fadingEdges(s1).horizontalScroll(s1),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FONT_NAMES.forEachIndexed { i, n -> Chip(n, t.font == i) { t.font = i } }
        }
        Text("Schriftfarbe", fontSize = 12.sp, color = Color.Black)
        Row(
            Modifier.fillMaxWidth().fadingEdges(s2).horizontalScroll(s2),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PALETTE.forEach { c -> ColorDot(c, t.color == c) { t.color = c } }
        }
        Text("Textfeld-Hintergrund", fontSize = 12.sp, color = Color.Black)
        Row(
            Modifier.fillMaxWidth().fadingEdges(s3).horizontalScroll(s3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Chip("Kein", t.bg == null) { t.bg = null }
            PALETTE.forEach { c -> ColorDot(c, t.bg == c) { t.bg = c } }
        }
        Text("Abgerundete Ecken", fontSize = 12.sp, color = Color.Black)
        SliderRow({ CornerIcon(t.corner) }, t.corner, { t.corner = it })
        Text("Ausrichtung", fontSize = 12.sp, color = Color.Black)
        Row(
            Modifier.fillMaxWidth().fadingEdges(s4).horizontalScroll(s4),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Chip("Linksbündig", t.align == 0) { t.align = 0 }
            Chip("Mittig", t.align == 1) { t.align = 1 }
            Chip("Rechtsbündig", t.align == 2) { t.align = 2 }
        }
        Text("Transparenz", fontSize = 12.sp, color = Color.Black)
        SliderRow({ TransparencyIcon(t.transp) }, t.transp, { t.transp = it })
        Text("Zeilenabstand", fontSize = 12.sp, color = Color.Black)
        SliderRow({ DoubleArrowIcon(true) }, t.lineSp, { t.lineSp = it })
        Text("Buchstabenabstand", fontSize = 12.sp, color = Color.Black)
        SliderRow({ DoubleArrowIcon(false) }, t.letterSp, { t.letterSp = it })
        Text("Zeilenkrümmung", fontSize = 12.sp, color = Color.Black)
        SliderRow({ CurveIcon(t.curve) }, t.curve, { t.curve = it }, -100, 100)
    }
}

/* ---------- Rahmen-Panel ---------- */

@Composable
private fun FormatRow(state: EditorState) {
    val idx = state.ratioIdx.coerceIn(0, RATIOS.size - 1)
    val sc = rememberScrollState()
    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) {
            Text(
                RATIOS[idx].label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                softWrap = false
            )
        }
        Row(
            Modifier.weight(1f).fadingEdges(sc).horizontalScroll(sc),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RATIOS.forEachIndexed { i, r ->
                Text(
                    r.label,
                    fontSize = 16.sp,
                    color = if (i == idx) Color.Black else Grau,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .clickable {
                            state.ratioIdx = i
                            val (w, h) = canvasSize(state)
                            state.texts.forEach { t ->
                                t.x = t.x.coerceIn(0f, max(0f, w - t.w))
                                t.y = t.y.coerceIn(0f, max(0f, h - 100f))
                            }
                        }
                        .padding(horizontal = 4.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun FramePanel(state: EditorState) {
    val cs = rememberScrollState()
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F4F4))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        if (state.mode != Mode.REIHE) {
            FormatRow(state)
        }
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) { ThicknessIcon(state.gap) }
            FrameSlider(state.gap, { state.gap = it }, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) { RoundnessIcon(state.rundung) }
            FrameSlider(state.rundung, { state.rundung = it }, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) { ColorSquareIcon(state.frameColor) }
            Row(
                Modifier.weight(1f).fadingEdges(cs).horizontalScroll(cs),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PALETTE.forEach { c -> ColorDot(c, state.frameColor == c) { state.frameColor = c } }
            }
        }
    }
}

@Composable
fun GridPicker(state: EditorState) {
    val opts = collageOptions(state.images.size)
    val (cw, ch) = canvasSize(state)
    val f = 100f / max(cw, ch)
    val tw = cw * f
    val th = ch * f
    val sc = rememberScrollState()
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .fadingEdges(sc)
            .horizontalScroll(sc)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (opts.isEmpty()) {
            Text("Füge zuerst Bilder hinzu.", color = Color.Black)
        }
        opts.forEachIndexed { i, o ->
            val isSel = i == state.collageIdx.coerceIn(0, opts.size - 1)
            val cells = collageCells(o, tw, th, 4f)
            Canvas(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE0E0E0))
                    .then(
                        if (isSel) Modifier.border(3.dp, Teal, RoundedCornerShape(6.dp)) else Modifier
                    )
                    .clickable {
                        state.collageIdx = i
                        state.gridOpen = false
                    }
            ) {
                val k = size.width / 100f
                val offX = (100f - tw) / 2f * k
                val offY = (100f - th) / 2f * k
                drawRect(Color(0xFFCCCCCC), topLeft = Offset(offX, offY), size = Size(tw * k, th * k))
                for (c in cells) {
                    drawRect(
                        Color(0xFF7CC6D4),
                        topLeft = Offset(offX + c.x * k, offY + c.y * k),
                        size = Size(c.w * k, c.h * k)
                    )
                }
            }
        }
    }
}

/* ---------- Bildauflösung ---------- */

@Composable
fun ResolutionDialog(current: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
        ) {
            Text(
                "←",
                fontSize = 30.sp,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clickable { onDismiss() }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 70.dp, bottom = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QUALITY_NAMES.forEachIndexed { i, n ->
                    val sel = i == current
                    Text(
                        n,
                        fontSize = 24.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                        color = if (sel) Color.Black else Grau,
                        modifier = Modifier
                            .clickable { onSelect(i) }
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/* ---------- Untere Leiste ---------- */

@Composable
fun BarButton(label: String, size: Int, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier
            .height(64.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.55f))
            .border(4.dp, Color.White, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = size.sp,
            color = Color.Black,
            fontFamily = RussoFamily,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun Pip(on: Boolean) {
    Box(
        Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (on) Teal else Color(0xFFDDDDDD))
    )
}

@Composable
fun FormatButton(state: EditorState, modifier: Modifier) {
    val shape = RoundedCornerShape(22.dp)
    val v = state.vertical
    Box(
        modifier
            .height(64.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.55f))
            .border(4.dp, Color.White, shape)
            .clickable {
                state.vertical = !state.vertical
                val (w, h) = canvasSize(state)
                state.texts.forEach { t ->
                    t.x = t.x.coerceIn(0f, max(0f, w - t.w))
                    t.y = t.y.coerceIn(0f, max(0f, h - 100f))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                Modifier
                    .size(if (v) 18.dp else 28.dp, if (v) 28.dp else 18.dp)
                    .background(Color(0xFFBBBBBB))
                    .border(2.dp, Color.White)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pip(v)
                Pip(!v)
            }
        }
    }
}

@Composable
fun BottomBar(state: EditorState, onAdd: () -> Unit, onText: () -> Unit, onFrame: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFE6E6E6))
            .navigationBarsPadding()
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when (state.mode) {
            Mode.FREISTIL -> {
                BarButton("+", 34, Modifier.weight(1f), onAdd)
                BarButton("Rahmen", 14, Modifier.weight(1f), onFrame)
                BarButton("Text", 22, Modifier.weight(1f), onText)
            }
            Mode.REIHE -> {
                FormatButton(state, Modifier.weight(1f))
                BarButton("Rahmen", 14, Modifier.weight(1f), onFrame)
                BarButton("+", 34, Modifier.weight(1f), onAdd)
                BarButton("Text", 20, Modifier.weight(1f), onText)
            }
            Mode.COLLAGE -> {
                BarButton("+", 34, Modifier.weight(1f), onAdd)
                BarButton("Rahmen", 14, Modifier.weight(1f), onFrame)
                BarButton("Raster", 16, Modifier.weight(1f)) {
                    state.gridOpen = !state.gridOpen
                    if (state.gridOpen) state.frameOpen = false
                }
                BarButton("Text", 20, Modifier.weight(1f), onText)
            }
        }
    }
}
