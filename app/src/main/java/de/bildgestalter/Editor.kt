package de.bildgestalter

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val Teal = Color(0xFF3AA8C1)

private val PALETTE = listOf(
    Color.Black, Color.White, Color(0xFFE53935), Color(0xFFFB8C00),
    Color(0xFFFDD835), Color(0xFF43A047), Color(0xFFB5ECF2), Color(0xFF1E88E5),
    Color(0xFF8E24AA), Color(0xFFEC407A), Color(0xFF757575), Color(0xFF6D4C41)
)

private fun familyOf(i: Int): FontFamily = when (i) {
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

@Composable
fun EditorScreen(state: EditorState, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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
        var x = (cw - 540f) / 2f
        var y = ch * 0.4f
        if (state.mode == Mode.REIHE) {
            val sc = scaleRef[0]
            if (state.vertical) {
                y = vScroll.value / sc + 120f
            } else {
                x = hScroll.value / sc + 120f
            }
        }
        state.addText(x, y)
    }

    val export: () -> Unit = {
        if (state.images.isEmpty() && state.texts.isEmpty()) {
            Toast.makeText(context, "Bitte zuerst Bilder oder Text hinzufügen.", Toast.LENGTH_SHORT).show()
        } else {
            state.selected = null
            scope.launch {
                val r = exportImage(context, state)
                val msg = if (r != null) "Gespeichert als $r in der Galerie (Ordner Bildgestalter)"
                else "Export fehlgeschlagen"
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
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
                "←", fontSize = 30.sp, color = Teal,
                modifier = Modifier.clickable { onBack() }.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Text(
                "Exportieren", fontSize = 18.sp, color = Teal,
                modifier = Modifier.clickable { export() }.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFC8C8C8))
        ) {
            val aw = constraints.maxWidth.toFloat()
            val ah = constraints.maxHeight.toFloat()
            val (cw, ch) = canvasSize(state)
            val scale = when {
                state.mode != Mode.REIHE -> min(aw * 0.94f / cw, ah * 0.94f / ch)
                state.vertical -> aw * 0.84f / cw
                else -> ah * 0.84f / ch
            }
            scaleRef[0] = scale

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
                    Stage(state, cw, ch, scale)
                } else if (state.vertical) {
                    Column(
                        Modifier.fillMaxWidth().verticalScroll(vScroll),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Stage(state, cw, ch, scale)
                        Spacer(Modifier.height(16.dp))
                    }
                } else {
                    Row(
                        Modifier.fillMaxHeight().horizontalScroll(hScroll),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(16.dp))
                        Stage(state, cw, ch, scale)
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
}

@Composable
fun Stage(state: EditorState, cw: Float, ch: Float, scale: Float) {
    val density = LocalDensity.current
    val rects = rectsOf(state)
    val free = state.mode == Mode.FREISTIL
    val sw = (cw * scale).roundToInt()
    val sh = (ch * scale).roundToInt()
    val stageBg = if (free) Color.White else state.frameColor
    Box(
        Modifier
            .size(with(density) { sw.toDp() }, with(density) { sh.toDp() })
            .background(stageBg)
    ) {
        if (state.images.isEmpty() && state.texts.isEmpty()) {
            Text(
                when (state.mode) {
                    Mode.FREISTIL -> "Füge Bilder oder Text hinzu"
                    Mode.COLLAGE -> "Tippe auf „+“"
                    Mode.REIHE -> "Füge Bilder hinzu"
                },
                color = Color(0xFF999999),
                modifier = Modifier.align(Alignment.Center)
            )
        }
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
                        x = x0.toFloat(), y = y0.toFloat(),
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
                    x = t.x * scale, y = t.y * scale, w = t.w * scale, h = null,
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
                        color = t.color,
                        fontSize = with(density) { fsPx.toSp() },
                        fontFamily = familyOf(t.font),
                        fontWeight = weightOf(t.font),
                        fontStyle = styleOf(t.font),
                        textAlign = alignOf(t.align)
                    )
                }
            }
        }
    }
}

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
    val padPx = with(density) { 22.dp.roundToPx() }
    val padDp = with(density) { padPx.toDp() }
    val curSelect by rememberUpdatedState(onSelect)
    val curMove by rememberUpdatedState(onMove)
    val curResize by rememberUpdatedState(onResize)
    val curDelete by rememberUpdatedState(onDelete)
    val totalW = with(density) { (w.roundToInt() + 2 * padPx).toDp() }
    val sizeMod = if (h != null) {
        Modifier.size(totalW, with(density) { (h.roundToInt() + 2 * padPx).toDp() })
    } else {
        Modifier.width(totalW)
    }

    Box(
        Modifier
            .offset { IntOffset(x.roundToInt() - padPx, y.roundToInt() - padPx) }
            .then(sizeMod)
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
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .pointerInput(Unit) { detectTapGestures(onTap = { curDelete() }) },
                contentAlignment = Alignment.Center
            ) {
                Text("×", color = Color.White, fontSize = 24.sp)
            }
            if (resizable) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFF999999), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, drag ->
                                change.consume()
                                curResize((drag.x + drag.y) / 2f)
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
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                if (selected) 3.dp else 1.dp,
                if (selected) Teal else Color(0xFF888888),
                CircleShape
            )
            .clickable { onClick() }
    )
}

@Composable
fun TextPanel(t: TxtItem) {
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
        Text("Schriftart", fontSize = 12.sp, color = Color.Gray)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FONT_NAMES.forEachIndexed { i, n -> Chip(n, t.font == i) { t.font = i } }
        }
        Text("Schriftfarbe", fontSize = 12.sp, color = Color.Gray)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PALETTE.forEach { c -> ColorDot(c, t.color == c) { t.color = c } }
        }
        Text("Textfeld-Hintergrund", fontSize = 12.sp, color = Color.Gray)
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Chip("Kein", t.bg == null) { t.bg = null }
            PALETTE.forEach { c -> ColorDot(c, t.bg == c) { t.bg = c } }
        }
        Text("Ausrichtung", fontSize = 12.sp, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Linksbündig", t.align == 0) { t.align = 0 }
            Chip("Mittig", t.align == 1) { t.align = 1 }
            Chip("Rechtsbündig", t.align == 2) { t.align = 2 }
        }
    }
}

/* ---------- Rahmen-Panel ---------- */

@Composable
private fun ThicknessIcon() {
    Canvas(Modifier.size(28.dp)) {
        val sw = 2.dp.toPx()
        val c = Color(0xFF444444)
        drawRoundRect(
            c, Offset(sw / 2f, sw / 2f),
            Size(size.width - sw, size.height - sw),
            CornerRadius(3.dp.toPx()), Stroke(sw)
        )
        val y = size.height * 0.4f
        drawLine(c, Offset(0f, y), Offset(size.width, y), sw)
        drawLine(c, Offset(size.width / 2f, y), Offset(size.width / 2f, size.height), sw)
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
private fun ColorSquareIcon(color: Color) {
    Box(
        Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .border(1.5.dp, Color(0xFF444444), RoundedCornerShape(4.dp))
    )
}

@Composable
private fun FrameSlider(value: Int, onChange: (Int) -> Unit, modifier: Modifier) {
    Slider(
        value = value.toFloat(),
        onValueChange = { onChange(it.roundToInt()) },
        valueRange = 0f..100f,
        steps = 99,
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
fun FramePanel(state: EditorState) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F4F4))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) { ThicknessIcon() }
            FrameSlider(state.gap, { state.gap = it }, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) { RoundnessIcon(state.rundung) }
            FrameSlider(state.rundung, { state.rundung = it }, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(44.dp), contentAlignment = Alignment.CenterStart) { ColorSquareIcon(state.frameColor) }
            Row(
                Modifier.weight(1f).horizontalScroll(rememberScrollState()),
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
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (opts.isEmpty()) {
            Text("Füge zuerst Bilder hinzu.", color = Color.Gray)
        }
        opts.forEachIndexed { i, o ->
            val isSel = i == state.collageIdx.coerceIn(0, opts.size - 1)
            val cells = collageCells(o, 100f, 4f)
            Canvas(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFCCCCCC))
                    .then(
                        if (isSel) Modifier.border(3.dp, Teal, RoundedCornerShape(6.dp)) else Modifier
                    )
                    .clickable {
                        state.collageIdx = i
                        state.gridOpen = false
                    }
            ) {
                val k = size.width / 100f
                for (c in cells) {
                    drawRect(
                        Color(0xFF7CC6D4),
                        topLeft = Offset(c.x * k, c.y * k),
                        size = Size(c.w * k, c.h * k)
                    )
                }
            }
        }
    }
}

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
        OutlinedLabel(label, size)
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
