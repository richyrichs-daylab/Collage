package de.bildgestalter

import android.graphics.RectF
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private val Teal = Color(0xFF3AA8C1)
private val Grau = Color(0xFFA8A8A8)

// Zusätzlicher Rand um die Reihe-Fläche, damit die Eckkreise Platz haben
private val ITEM_PAD = 22.dp

private val PALETTE = listOf(
    Color.Black, Color.White, Color(0xFFE53935), Color(0xFFFB8C00),
    Color(0xFFFDD835), Color(0xFF43A047), Color(0xFFB5ECF2), Color(0xFF1E88E5),
    Color(0xFF8E24AA), Color(0xFFEC407A), Color(0xFF757575), Color(0xFF6D4C41),
    GOLD, SILVER
)

/** Übersetzte Schriftnamen (Russo One und Serif bleiben, wie sie sind). */
private fun fontLabel(i: Int, n: String): String = when (i) {
    2 -> tr(S_TYPEW)
    3 -> tr(S_HAND)
    4 -> tr(S_BOLD)
    5 -> tr(S_ITAL)
    else -> n
}

/** Kleiner Verlauf für Farbfelder (Punkte und Icons) in den Einstellungen. */
private fun swatchBrush(c: Color): Brush = when (glitterKind(c)) {
    1 -> Brush.linearGradient(
        listOf(Color(0xFFB8842A), Color(0xFFD6AB3B), Color(0xFFEBD36B), Color(0xFFD3AA36))
    )
    2 -> Brush.verticalGradient(
        listOf(Color(0xFFB2B2B2), Color(0xFFF2F2F2), Color(0xFFD4D4D4), Color(0xFFBCBCBC))
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
    var busy by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(msg) {
        if (msg != null) {
            delay(3500)
            msg = null
        }
    }

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
        val t = TxtItem()
        t.text = tr(S_TEXT)
        val g = textGeom(t)
        val (cw, ch) = canvasSize(state)
        var x = (cw - g.w) / 2f
        var y = (ch - g.h) / 2f
        if (state.mode == Mode.REIHE) {
            val sc = scaleRef[0]
            if (state.vertical) {
                y = vScroll.value / sc + 120f
            } else {
                x = hScroll.value / sc + 120f
            }
        }
        t.x = x
        t.y = y
        state.texts.add(t)
        state.select(t)
    }

    val export: () -> Unit = {
        if (!busy) {
            if (state.images.isEmpty() && state.texts.isEmpty()) {
                Toast.makeText(context, tr(S_EMPTY), Toast.LENGTH_SHORT).show()
            } else {
                busy = true
                msg = null
                state.selected = null
                scope.launch {
                    val r = exportImage(context, state)
                    busy = false
                    msg = if (r != null) tr(S_SAVED).replace("%s", r)
                    else tr(S_FAIL)
                }
            }
        }
    }

    val toggleFrame: () -> Unit = {
        state.frameOpen = !state.frameOpen
        if (state.frameOpen) state.selected = null
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFE6E6E6))
    ) {
        Column(
            Modifier
                .fillMaxSize()
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
                        tr(S_RES), fontSize = 15.sp, color = Color.Black, maxLines = 2,
                        modifier = Modifier.clickable { showRes = true }.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                    Text(
                        tr(S_EXPORT), fontSize = 15.sp, color = Color.Black, maxLines = 2,
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
                            detectTapGestures(onTap = { state.selected = null })
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
            if (state.frameOpen) FramePanel(state)
            BottomBar(pick, addText, toggleFrame)
        }

        val m = msg
        if (busy || m != null) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 100.dp)
            ) {
                MessagePill(busy, m)
            }
        }
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

/* ---------- Meldung mit blinkenden Punkten ---------- */

@Composable
private fun BlinkingDots() {
    val tr = rememberInfiniteTransition(label = "dots")
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..2) {
            val a by tr.animateFloat(
                initialValue = 0.05f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(650, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(i * 220)
                ),
                label = "dot$i"
            )
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = a))
            )
        }
    }
}

@Composable
private fun MessagePill(busy: Boolean, msg: String?) {
    Row(
        Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xE6303030))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (busy) {
            Text(tr(S_EXPORTING), color = Color.White, fontSize = 15.sp)
            Spacer(Modifier.width(8.dp))
            BlinkingDots()
        } else if (msg != null) {
            Text(msg, color = Color.White, fontSize = 15.sp)
        }
    }
}

/* ---------- Fläche (Vorschau, Ausgrauen, Griffe, Gesten) ---------- */

private class ViewInfo(val scale: Float, val ox: Float, val oy: Float)

/**
 * ox, oy: Position des weißen Feldes im Container (in Pixeln).
 * boxW, boxH: Größe des Containers (in Pixeln).
 * Alles wird in einem Zug gezeichnet: erst die Szene (wie im Export), dann die
 * graue Schicht außerhalb des Feldes, dann Auswahlrahmen und Eckkreise.
 */
@Composable
fun Stage(
    state: EditorState, cw: Float, ch: Float, scale: Float,
    ox: Int, oy: Int, boxW: Int, boxH: Int
) {
    val density = LocalDensity.current
    val dp = density.density
    val view = rememberUpdatedState(ViewInfo(scale, ox.toFloat(), oy.toFloat()))
    val hitR = 24f * dp
    val dimPaint = remember {
        android.graphics.Paint().apply { color = android.graphics.Color.argb(128, 200, 200, 200) }
    }

    Canvas(
        Modifier
            .size(with(density) { boxW.toDp() }, with(density) { boxH.toDp() })
            .pointerInput(state) { stageGestures(state, view, hitR) }
    ) {
        val nc = drawContext.canvas.nativeCanvas
        val fx = ox.toFloat()
        val fy = oy.toFloat()

        nc.save()
        nc.translate(fx, fy)
        nc.scale(scale, scale)
        drawScene(nc, state, { img, _ -> img.bmp }, { _, _ -> })
        nc.restore()

        // Alles außerhalb des weißen Feldes: 50 % graue Schicht
        nc.save()
        nc.clipOutRect(fx, fy, fx + cw * scale, fy + ch * scale)
        nc.drawRect(0f, 0f, size.width, size.height, dimPaint)
        nc.restore()

        drawSelection(nc, state, fx, fy, scale, dp)
    }
}

private fun nPaint() = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

private fun drawSelection(
    nc: android.graphics.Canvas, state: EditorState,
    ox: Float, oy: Float, scale: Float, dp: Float
) {
    val sel = state.selected ?: return
    val f = frameOf(state, sel) ?: return
    val free = state.mode == Mode.FREISTIL

    // Auswahlrahmen (folgt Rundung, Krümmung und Drehung)
    nc.save()
    nc.translate(ox, oy)
    nc.scale(scale, scale)
    nc.translate(f.cx, f.cy)
    nc.rotate(f.rot)
    nc.translate(-f.w / 2f, -f.h / 2f)
    val sp = nPaint()
    sp.style = android.graphics.Paint.Style.STROKE
    sp.strokeWidth = 2f * dp / scale
    sp.color = 0xFF3AA8C1.toInt()
    val path = f.path
    if (path == null) nc.drawRoundRect(0f, 0f, f.w, f.h, f.rad, f.rad, sp)
    else nc.drawPath(path, sp)
    nc.restore()

    fun px(q: Pt): Pt {
        val u = f.toUnit(q)
        return Pt(ox + u.x * scale, oy + u.y * scale)
    }

    val r = 18f * dp
    val d = px(f.tl)
    drawDeleteHandle(nc, d.x, d.y, r, dp)
    if (sel is TxtItem || free) {
        val rs = px(f.br)
        drawResizeHandle(nc, rs.x, rs.y, r, dp)
        val ro = px(f.bl)
        drawRotateHandle(nc, ro.x, ro.y, r, dp)
    }
}

private fun drawDeleteHandle(nc: android.graphics.Canvas, x: Float, y: Float, r: Float, dp: Float) {
    val p = nPaint()
    p.color = 0xFFE53935.toInt()
    nc.drawCircle(x, y, r, p)
    p.color = android.graphics.Color.WHITE
    p.style = android.graphics.Paint.Style.STROKE
    p.strokeWidth = 2.8f * dp
    p.strokeCap = android.graphics.Paint.Cap.ROUND
    val l = 6f * dp
    nc.drawLine(x - l, y - l, x + l, y + l, p)
    nc.drawLine(x - l, y + l, x + l, y - l, p)
}

private fun drawWhiteCircle(nc: android.graphics.Canvas, x: Float, y: Float, r: Float, dp: Float) {
    val p = nPaint()
    p.color = android.graphics.Color.WHITE
    nc.drawCircle(x, y, r, p)
    p.style = android.graphics.Paint.Style.STROKE
    p.strokeWidth = 1f * dp
    p.color = 0xFF999999.toInt()
    nc.drawCircle(x, y, r, p)
}

private fun drawResizeHandle(nc: android.graphics.Canvas, x: Float, y: Float, r: Float, dp: Float) {
    drawWhiteCircle(nc, x, y, r, dp)
    val p = nPaint()
    p.style = android.graphics.Paint.Style.STROKE
    p.strokeWidth = 2.5f * dp
    p.strokeCap = android.graphics.Paint.Cap.ROUND
    p.color = android.graphics.Color.BLACK
    val s = 18f * dp
    val x0 = x - s / 2f
    val y0 = y - s / 2f
    nc.drawLine(x0 + s * 0.15f, y0 + s * 0.15f, x0 + s * 0.85f, y0 + s * 0.85f, p)
    nc.drawLine(x0 + s * 0.85f, y0 + s * 0.85f, x0 + s * 0.45f, y0 + s * 0.85f, p)
    nc.drawLine(x0 + s * 0.85f, y0 + s * 0.85f, x0 + s * 0.85f, y0 + s * 0.45f, p)
    nc.drawLine(x0 + s * 0.15f, y0 + s * 0.15f, x0 + s * 0.55f, y0 + s * 0.15f, p)
    nc.drawLine(x0 + s * 0.15f, y0 + s * 0.15f, x0 + s * 0.15f, y0 + s * 0.55f, p)
}

private fun drawRotateHandle(nc: android.graphics.Canvas, x: Float, y: Float, r: Float, dp: Float) {
    drawWhiteCircle(nc, x, y, r, dp)
    val p = nPaint()
    p.style = android.graphics.Paint.Style.STROKE
    p.strokeWidth = 2.4f * dp
    p.strokeCap = android.graphics.Paint.Cap.ROUND
    p.color = android.graphics.Color.BLACK
    val ar = 7f * dp
    nc.drawArc(RectF(x - ar, y - ar, x + ar, y + ar), -40f, 270f, false, p)
    // Pfeilspitze am Ende des Bogens (Winkel 230 Grad, im Uhrzeigersinn)
    val a = Math.toRadians(230.0)
    val ex = x + ar * cos(a).toFloat()
    val ey = y + ar * sin(a).toFloat()
    val tx = -sin(a).toFloat()
    val ty = cos(a).toFloat()
    val nx = -ty
    val ny = tx
    val tri = android.graphics.Path()
    tri.moveTo(ex + tx * 5f * dp, ey + ty * 5f * dp)
    tri.lineTo(ex + nx * 3.6f * dp, ey + ny * 3.6f * dp)
    tri.lineTo(ex - nx * 3.6f * dp, ey - ny * 3.6f * dp)
    tri.close()
    p.style = android.graphics.Paint.Style.FILL
    nc.drawPath(tri, p)
}

/**
 * Alle Gesten auf der Fläche: Auswählen, Verschieben, Größe ändern, Drehen, Löschen.
 * Bilder in Collage und Reihe sind nicht verschiebbar; dort scrollt die Reihe weiter.
 */
private suspend fun PointerInputScope.stageGestures(
    state: EditorState, view: State<ViewInfo>, hitR: Float
) {
    val slop = viewConfiguration.touchSlop
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val v = view.value
        val p0 = down.position
        val u0 = Pt((p0.x - v.ox) / v.scale, (p0.y - v.oy) / v.scale)
        val free = state.mode == Mode.FREISTIL

        // 1 = verschieben, 2 = Größe, 3 = drehen, 4 = löschen
        var mode = 0
        var target: Any? = null
        var tapTarget: Any? = null

        val sel = state.selected
        if (sel != null) {
            val f = frameOf(state, sel)
            if (f != null) {
                val canTransform = sel is TxtItem || free
                fun near(q: Pt): Boolean {
                    val u = f.toUnit(q)
                    val d = Offset(v.ox + u.x * v.scale, v.oy + u.y * v.scale) - p0
                    return d.getDistance() <= hitR
                }
                if (near(f.tl)) mode = 4
                else if (canTransform && near(f.br)) mode = 2
                else if (canTransform && near(f.bl)) mode = 3
                if (mode != 0) target = sel
            }
        }
        if (mode == 0) {
            val hit = hitItem(state, u0, 8f / v.scale)
            if (hit != null) {
                tapTarget = hit
                if (hit is TxtItem || free) {
                    state.select(hit)
                    target = hit
                    mode = 1
                }
            }
        }
        if (mode != 0) down.consume()

        var rot0 = 0f
        var a0 = 0f
        var cxPx = 0f
        var cyPx = 0f
        if (mode == 3) {
            val f0 = target?.let { frameOf(state, it) }
            if (f0 != null) {
                cxPx = v.ox + f0.cx * v.scale
                cyPx = v.oy + f0.cy * v.scale
                a0 = atan2(p0.y - cyPx, p0.x - cxPx)
                rot0 = f0.rot
            } else {
                mode = 0
            }
        }

        var prev = p0
        var movedFar = false
        while (true) {
            val ev = awaitPointerEvent()
            val ch = ev.changes.firstOrNull { it.id == down.id } ?: break
            val pos = ch.position

            if (ch.changedToUpIgnoreConsumed()) {
                if (mode == 4) {
                    val t = target
                    val f = if (t != null) frameOf(state, t) else null
                    if (t != null && f != null) {
                        val u = f.toUnit(f.tl)
                        val d = Offset(v.ox + u.x * v.scale, v.oy + u.y * v.scale) - pos
                        if (d.getDistance() <= hitR * 1.4f) deleteItem(state, t)
                    }
                    ch.consume()
                } else if (mode == 0) {
                    if (!movedFar) {
                        state.select(tapTarget)
                        ch.consume()
                    }
                } else {
                    ch.consume()
                }
                break
            }

            if (mode == 0) {
                if (ch.isConsumed) break
                if ((pos - p0).getDistance() > slop) movedFar = true
            } else {
                when (mode) {
                    1 -> {
                        val dx = (pos.x - prev.x) / v.scale
                        val dy = (pos.y - prev.y) / v.scale
                        when (val t = target) {
                            is ImgItem -> { t.x += dx; t.y += dy }
                            is TxtItem -> { t.x += dx; t.y += dy }
                        }
                    }
                    2 -> {
                        val t = target
                        val f = if (t != null) frameOf(state, t) else null
                        if (t != null && f != null) {
                            val dxU = (pos.x - prev.x) / v.scale
                            val dyU = (pos.y - prev.y) / v.scale
                            val ia = Math.toRadians(-f.rot.toDouble())
                            val ics = cos(ia).toFloat()
                            val isn = sin(ia).toFloat()
                            val d = ((dxU * ics - dyU * isn) + (dxU * isn + dyU * ics)) / 2f
                            val minW = if (t is TxtItem) 40f else 60f
                            val nw = max(minW, f.w + d)
                            val k = nw / f.w
                            // Obere linke Ecke bleibt an ihrer Stelle
                            val tlU = f.toUnit(Pt(0f, 0f))
                            var w2 = f.w * k
                            var h2 = f.h * k
                            if (t is TxtItem) {
                                t.fs = t.fs * k
                                val g2 = textGeom(t)
                                w2 = g2.w
                                h2 = g2.h
                            } else if (t is ImgItem) {
                                t.w = w2
                                t.h = h2
                            }
                            val rr = Math.toRadians(f.rot.toDouble())
                            val c2 = cos(rr).toFloat()
                            val s2 = sin(rr).toFloat()
                            val ncx = tlU.x + (w2 / 2f) * c2 - (h2 / 2f) * s2
                            val ncy = tlU.y + (w2 / 2f) * s2 + (h2 / 2f) * c2
                            when (t) {
                                is ImgItem -> { t.x = ncx - w2 / 2f; t.y = ncy - h2 / 2f }
                                is TxtItem -> { t.x = ncx - w2 / 2f; t.y = ncy - h2 / 2f }
                            }
                        }
                    }
                    3 -> {
                        val ang = atan2(pos.y - cyPx, pos.x - cxPx)
                        var r = rot0 + Math.toDegrees((ang - a0).toDouble()).toFloat()
                        r = ((r % 360f) + 540f) % 360f - 180f
                        val nearest = (r / 45f).roundToInt() * 45f
                        if (abs(r - nearest) < 3f) r = nearest
                        when (val t = target) {
                            is ImgItem -> t.rot = r
                            is TxtItem -> t.rot = r
                        }
                    }
                }
                ch.consume()
            }
            prev = pos
        }
    }
}

/* ---------- Bausteine ---------- */

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
    )
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
        drawRect(c, Offset(sw, max(sw, y - tw / 2f)), Size(size.width - 2 * sw, tw))
        drawRect(c, Offset(x - tw / 2f, y), Size(tw, size.height - sw - y))
        drawRoundRect(
            c, Offset(sw / 2f, sw / 2f),
            Size(size.width - sw, size.height - sw),
            CornerRadius(3.dp.toPx()), style = Stroke(sw)
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
            CornerRadius(r), style = Stroke(sw)
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
private fun PadIcon(percent: Int) {
    Canvas(Modifier.size(28.dp)) {
        val tSize = 14.dp.toPx()
        val m = percent / 100f * 6.dp.toPx()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val half = tSize / 2f
        val tl = Offset(cx - half - m, cy - half - m)
        val sz = Size(tSize + 2 * m, tSize + 2 * m)
        // Rahmen (Hintergrundfläche) schmiegt sich an das T
        drawRoundRect(Color.White, tl, sz, CornerRadius(2.dp.toPx()))
        drawRoundRect(Color(0xFFBBBBBB), tl, sz, CornerRadius(2.dp.toPx()), style = Stroke(1.dp.toPx()))
        val bar = 3.dp.toPx()
        drawRect(Color.Black, Offset(cx - half, cy - half), Size(tSize, bar))
        drawRect(Color.Black, Offset(cx - bar / 2f, cy - half), Size(bar, tSize))
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

/* ---------- Textfeld-Bearbeitungsfeld ---------- */

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
            label = { Text(tr(S_TEXT)) },
            maxLines = 4
        )
        Text(tr(S_FONT), fontSize = 12.sp, color = Color.Black)
        Row(
            Modifier.fillMaxWidth().fadingEdges(s1).horizontalScroll(s1),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FONT_NAMES.forEachIndexed { i, n -> Chip(fontLabel(i, n), t.font == i) { t.font = i } }
        }
        Text(tr(S_FCOLOR), fontSize = 12.sp, color = Color.Black)
        Row(
            Modifier.fillMaxWidth().fadingEdges(s2).horizontalScroll(s2),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PALETTE.forEach { c -> ColorDot(c, t.color == c) { t.color = c } }
        }
        Text(tr(S_PADAREA), fontSize = 12.sp, color = Color.Black)
        SliderRow({ PadIcon(t.pad) }, t.pad, { t.pad = it })
        Text(tr(S_BG), fontSize = 12.sp, color = Color.Black)
        Row(
            Modifier.fillMaxWidth().fadingEdges(s3).horizontalScroll(s3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Chip(tr(S_NONE), t.bg == null) { t.bg = null }
            PALETTE.forEach { c -> ColorDot(c, t.bg == c) { t.bg = c } }
        }
        Text(tr(S_CORNERS), fontSize = 12.sp, color = Color.Black)
        SliderRow({ CornerIcon(t.corner) }, t.corner, { t.corner = it })
        Text(tr(S_ALIGN), fontSize = 12.sp, color = Color.Black)
        Row(
            Modifier.fillMaxWidth().fadingEdges(s4).horizontalScroll(s4),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Chip(tr(S_LEFT), t.align == 0) { t.align = 0 }
            Chip(tr(S_CENTER), t.align == 1) { t.align = 1 }
            Chip(tr(S_RIGHT), t.align == 2) { t.align = 2 }
        }
        Text(tr(S_TRANSP), fontSize = 12.sp, color = Color.Black)
        SliderRow({ TransparencyIcon(t.transp) }, t.transp, { t.transp = it })
        Text(tr(S_LINESP), fontSize = 12.sp, color = Color.Black)
        SliderRow({ DoubleArrowIcon(true) }, t.lineSp, { t.lineSp = it })
        Text(tr(S_LETSP), fontSize = 12.sp, color = Color.Black)
        SliderRow({ DoubleArrowIcon(false) }, t.letterSp, { t.letterSp = it })
        Text(tr(S_CURVE), fontSize = 12.sp, color = Color.Black)
        SliderRow({ CurveIcon(t.curve) }, t.curve, { t.curve = it }, -100, 100)
    }
}

/* ---------- Rahmen-Feld ---------- */

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
                            clampTexts(state)
                        }
                        .padding(horizontal = 4.dp, vertical = 12.dp)
                )
            }
        }
    }
}

/** Collage: Raster-Vorschauen, passend zur Bildanzahl und zum Format. */
@Composable
private fun RasterRow(state: EditorState) {
    val sc = rememberScrollState()
    val opts = collageOptions(state.images.size)
    if (opts.isEmpty()) return
    val (cw, ch) = canvasSize(state)
    val f = 100f / max(cw, ch)
    val tw = cw * f
    val th = ch * f
    val cur = state.collageIdx.coerceIn(0, opts.size - 1)
    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .fadingEdges(sc)
            .horizontalScroll(sc),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        opts.forEachIndexed { i, o ->
            val cells = collageCells(o, tw, th, 4f)
            Canvas(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFE0E0E0))
                    .then(
                        if (i == cur) Modifier.border(2.dp, Teal, RoundedCornerShape(5.dp)) else Modifier
                    )
                    .clickable { state.collageIdx = i }
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

@Composable
fun FramePanel(state: EditorState) {
    val cs = rememberScrollState()
    val maxH = if (state.mode == Mode.REIHE) 156.dp else 204.dp
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F4F4))
            .heightIn(max = maxH)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        if (state.mode == Mode.REIHE) {
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                FormatButton(state, Modifier.width(92.dp))
            }
        } else {
            FormatRow(state)
            if (state.mode == Mode.COLLAGE) RasterRow(state)
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
                    val label = when (i) {
                        1 -> tr(S_AVG)
                        2 -> tr(S_ECO)
                        else -> n
                    }
                    Text(
                        label,
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
    // Lange Wörter werden etwas verkleinert, damit sie in den Button passen
    val fs = if (label.length > 8) max(9f, size * 8f / label.length) else size.toFloat()
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
            fontSize = fs.sp,
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

/** Der Format-Button der Reihe (vertikal/horizontal), jetzt im Rahmen-Feld. */
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
                clampTexts(state)
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
fun BottomBar(onAdd: () -> Unit, onText: () -> Unit, onFrame: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFE6E6E6))
            .navigationBarsPadding()
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BarButton("+", 34, Modifier.weight(1f), onAdd)
        BarButton(tr(S_FRAME), 14, Modifier.weight(1f), onFrame)
        BarButton(tr(S_TEXT), 22, Modifier.weight(1f), onText)
    }
}
