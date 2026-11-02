package de.bildgestalter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Screen { HOME, FREISTIL, COLLAGE, REIHE }

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
    var screen by remember { mutableStateOf(Screen.HOME) }

    BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

    when (screen) {
        Screen.HOME -> HomeScreen(onOpen = { screen = it })
        else -> EditorScreen(
            title = when (screen) {
                Screen.FREISTIL -> "Freistil"
                Screen.COLLAGE -> "Collage"
                else -> "Reihe"
            },
            onBack = { screen = Screen.HOME }
        )
    }
}

@Composable
fun OutlinedLabel(text: String, size: Int) {
    Box {
        Text(
            text = text,
            style = TextStyle(
                fontSize = size.sp,
                color = Color.White,
                drawStyle = Stroke(width = 14f, join = StrokeJoin.Round)
            )
        )
        Text(text = text, style = TextStyle(fontSize = size.sp, color = Cyan))
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
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(34.dp))
                .then(
                    Modifier.background(Color.Transparent, RoundedCornerShape(34.dp))
                )
        )
        OutlinedLabel(text, 38)
    }
}

@Composable
fun HomeScreen(onOpen: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Blau, Gruen))),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BigButton("Freistil") { onOpen(Screen.FREISTIL) }
        BigButton("Collage") { onOpen(Screen.COLLAGE) }
        BigButton("Reihe") { onOpen(Screen.REIHE) }
    }
}

@Composable
fun EditorScreen(title: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFE6E6E6))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("←", fontSize = 30.sp, color = Color(0xFF2A9DB0),
                modifier = Modifier.clickable { onBack() })
            Text("Exportieren", fontSize = 17.sp, color = Color(0xFF2A9DB0),
                modifier = Modifier.clickable { /* kommt in Etappe 2 */ })
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("$title-Seite (kommt in der nächsten Etappe)", fontSize = 18.sp)
        }
    }
}
