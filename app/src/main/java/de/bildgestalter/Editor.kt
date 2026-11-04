package de.bildgestalter

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val Teal = Color(0xFF3AA8C1)
private val Grau = Color(0xFFA8A8A8)

// Zusätzlicher Rand um jedes Element für die Eckkreise (x und Pfeil)
private val ITEM_PAD = 22.dp

private val PALETTE = listOf(
    Color.Black, Color.White, Color(0xFFE53935), Color(0xFFFB8C00),
    Color(0xFFFDD835), Color(0xFF43A047), Color(0xFFB5ECF2), Color(0xFF1E88E5),
    Color(0xFF8E24AA), Color(0xFFEC407A), Color(0xFF757575), Color(0xFF6D4C41)
)

private fun familyOf(i: Int): FontFamily = when (i) {
    0 -> RussoFamily
    1 -> FontFamily.Serif
    2 -> FontFamily.Monospace
    3 -> FontFamily.Cursive
    else -> FontFamily.SansSerif
}

private fun weightOf(i: Int) = if (i == 4) FontWeight.Bold else FontWeight.Normal
private fun styleOf(i: Int) = if (i == 5) FontStyle.Italic else FontStyle.Normal
private fun alignOf(i: Int) = when (i) {
    0 -> TextAlign.Start
    2 -> TextAlign.End
    else -> TextAlign.Center
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
    val stageBg = if (free) Color.White else state.frameColor

    Box(Modifier.size(with(density) { boxW.toDp() }, with(density) { boxH.toDp() })) {
        // Das Feld (leer bleibt es ohne Hinweistext)
        Box(
            Modifier
                .offset { IntOffset(ox, oy) }
                .size(with(density) { sw.toDp() }, with(density) { sh.toDp() })
                .background(stageBg)
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
                                        Modifier.border(with(density) { borderPx.toDp() }, state.frameColor, shape)
                                    else Modifier
                                )
                        )
                    }
                }
            }
        }
        state.texts.forEach { t ->
            key(t) {
                ItemFrame(
                    x = ox + t.x * scale, y = oy + t.y * scale, w = t.w * scale, h = null,
                    selected = state.selected === t,
                    movable = true, resizable = true,
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
                ) {
                    val fsPx = t.fs * scale
                    Text(
                        text = if (t.text.isEmpty()) " " else t.text,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(t.bg ?: Color.Transparent)
                            .padding(with(density) { (fsPx * 0.25f).toDp() }),
                        style = TextStyle(
                            color = t.color,
                            fontSize = with(density) { fsPx.toSp() },
                            fontFamily = familyOf(t.font),
                            fontWeight = weightOf(t.font),
                            fontStyle = styleOf(t.font),
                            textAlign = alignOf(t.align),
                            lineHeight = TextUnit.Unspecified,
                            letterSpacing = TextUnit.Unspecified
                        )
                    )
                }
            }
        }
    }
}

/**
 * x, y: Position der linken oberen Ecke des Inhalts im Container (Pixel).
 * w, h: Größe des Inhalts in Pixeln (h = null: Höhe folgt dem Inhalt).
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
    val curResize by rememberUpdatedState(onResize)
    val curDelete by rememberUpdatedState(onDelete)
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
