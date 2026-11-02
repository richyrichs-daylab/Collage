package de.bildgestalter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private val Cyan = Color(0xFFB5ECF2)
private val Blau = Color(0xFFC2D4FF)
private val Gruen = Color(0xFFE3F7BE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var editor by remember { mutableStateOf<EditorState?>(null) }
    var confirmBack by remember { mutableStateOf(false) }

    val rowPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val bmps = loadBitmaps(context, uris)
                if (bmps.isNotEmpty()) {
                    val s = EditorState(Mode.REIHE)
                    s.addImages(bmps)
                    editor = s
                }
            }
        }
    }

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
            onFreistil = { editor = EditorState(Mode.FREISTIL) },
            onCollage = { editor = EditorState(Mode.COLLAGE) },
            onReihe = {
                rowPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
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

@Composable
fun OutlinedLabel(text: String, size: Int) {
    val r = max(1.5f, size * 0.09f)
    Box {
        for (i in 0 until 8) {
            val a = (i * Math.PI / 4).toFloat()
            Text(
                text = text,
                fontSize = size.sp,
                color = Color.White,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.offset((cos(a) * r).dp, (sin(a) * r).dp)
            )
        }
        Text(text = text, fontSize = size.sp, color = Cyan, maxLines = 1, softWrap = false)
    }
}

@Composable
fun BigButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(110.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(Color.White.copy(alpha = 0.45f))
            .border(6.dp, Color.White, RoundedCornerShape(34.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        OutlinedLabel(text, 38)
    }
}

@Composable
fun HomeScreen(onFreistil: () -> Unit, onCollage: () -> Unit, onReihe: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Blau, Gruen))),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BigButton("Freistil", onFreistil)
        BigButton("Collage", onCollage)
        BigButton("Reihe", onReihe)
    }
}
