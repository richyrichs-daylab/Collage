package de.bildgestalter

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val RussoFamily = FontFamily(Font(R.font.russo_one))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        russoTypeface = try {
            resources.getFont(R.font.russo_one)
        } catch (e: Throwable) {
            null
        }
        setContent { AppTheme { App() } }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val b = Typography()
    val f = RussoFamily
    val typo = Typography(
        displayLarge = b.displayLarge.copy(fontFamily = f),
        displayMedium = b.displayMedium.copy(fontFamily = f),
        displaySmall = b.displaySmall.copy(fontFamily = f),
        headlineLarge = b.headlineLarge.copy(fontFamily = f),
        headlineMedium = b.headlineMedium.copy(fontFamily = f),
        headlineSmall = b.headlineSmall.copy(fontFamily = f),
        titleLarge = b.titleLarge.copy(fontFamily = f),
        titleMedium = b.titleMedium.copy(fontFamily = f),
        titleSmall = b.titleSmall.copy(fontFamily = f),
        bodyLarge = b.bodyLarge.copy(fontFamily = f),
        bodyMedium = b.bodyMedium.copy(fontFamily = f),
        bodySmall = b.bodySmall.copy(fontFamily = f),
        labelLarge = b.labelLarge.copy(fontFamily = f),
        labelMedium = b.labelMedium.copy(fontFamily = f),
        labelSmall = b.labelSmall.copy(fontFamily = f)
    )
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF3AA8C1),
            onSurface = Color.Black,
            onSurfaceVariant = Color.Black,
            onBackground = Color.Black
        ),
        typography = typo
    ) {
        ProvideTextStyle(TextStyle(fontFamily = f, color = Color.Black), content)
    }
}

fun newEditor(context: Context, mode: Mode): EditorState {
    val s = EditorState(mode)
    s.quality = loadQuality(context, mode)
    return s
}

@Composable
fun App() {
    val context = LocalContext.current
    var editor by remember { mutableStateOf<EditorState?>(null) }
    var confirmBack by remember { mutableStateOf(false) }

    val current = editor
    val goBack: () -> Unit = {
        val c = editor
        if (c != null) {
            if (c.images.isEmpty() && c.texts.isEmpty()) editor = null else confirmBack = true
        }
    }

    BackHandler(enabled = current != null) { goBack() }

    if (current == null) {
        HomeScreen(
            onFreistil = { editor = newEditor(context, Mode.FREISTIL) },
            onCollage = { editor = newEditor(context, Mode.COLLAGE) },
            onReihe = { editor = newEditor(context, Mode.REIHE) }
        )
    } else {
        EditorScreen(current, goBack)
    }

    if (confirmBack) {
        AlertDialog(
            onDismissRequest = { confirmBack = false },
            title = { Text("Zur Startseite?") },
            text = { Text("Nicht exportierte Änderungen gehen verloren.") },
            confirmButton = {
                TextButton(onClick = { confirmBack = false; editor = null }) { Text("Ja") }
            },
            dismissButton = {
                TextButton(onClick = { confirmBack = false }) { Text("Abbrechen") }
            }
        )
    }
}

private fun DrawScope.drawHomeBackground() {
    val w = size.width
    val h = size.height

    // Grundverlauf: Orange oben links, Violett nach rechts
    drawRect(
        Brush.linearGradient(
            listOf(Color(0xFFD9800F), Color(0xFF9A4A66), Color(0xFF3E0F8C)),
            start = Offset(0f, 0f),
            end = Offset(w, h * 0.45f)
        )
    )
    // Unterer Bereich: helles Violett
    drawRect(
        Brush.verticalGradient(
            listOf(Color.Transparent, Color(0xFF8052FF), Color(0xFF6A3DD2)),
            startY = h * 0.35f,
            endY = h
        )
    )

    // Großer Kreis oben rechts (Magenta mit orangem Rand)
    val c1 = Offset(w * 1.47f, h * 0.338f)
    val r1 = w * 0.863f
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFE0207F), Color(0xFFA010A8), Color(0xFF5A0BB8)),
            center = Offset(c1.x - r1 * 0.6f, c1.y),
            radius = r1 * 1.4f
        ),
        radius = r1,
        center = c1
    )
    drawCircle(
        color = Color(0xFFF08A10),
        radius = r1,
        center = c1,
        style = Stroke(width = w * 0.012f)
    )

    // Großer Kreis unten links (Dunkelblau mit türkisem Rand)
    val c2 = Offset(-w * 0.39f, h * 0.854f)
    val r2 = w * 1.0f
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFF060040), Color(0xFF0A1A9E), Color(0xFF1E6FE0)),
            center = Offset(c2.x, c2.y + r2 * 0.3f),
            radius = r2 * 1.3f
        ),
        radius = r2,
        center = c2
    )
    drawCircle(
        color = Color(0xFF00E5E5),
        radius = r2,
        center = c2,
        style = Stroke(width = w * 0.014f)
    )
}

@Composable
fun HomeButton(text: String, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(alpha = 0.6f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 56.sp,
            color = Color.Black,
            fontFamily = RussoFamily,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun HomeScreen(onFreistil: () -> Unit, onCollage: () -> Unit, onReihe: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) { drawHomeBackground() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeButton("Freistil", Color(0xFFFF8A65), onFreistil)
            HomeButton("Collage", Color(0xFF7C4DFF), onCollage)
            HomeButton("Reihe", Color(0xFF64B5F6), onReihe)
        }
    }
}
