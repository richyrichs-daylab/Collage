package de.bildgestalter

// Beschreibungen für TalkBack. Jede Zeile hat genau 30 Felder:
// 0 Zurück, 1 Bilder hinzufügen, 2 Löschen, 3 Größer, 4 Kleiner, 5 Nach links drehen,
// 6 Nach rechts drehen, 7 Nach oben verschieben, 8 Nach unten verschieben,
// 9 Nach links verschieben, 10 Nach rechts verschieben, 11 Dicke, 12 Format, 13 Raster,
// 14 Vertikal, 15 Horizontal, 16 bis 29 Farben: Schwarz, Weiß, Rot, Orange, Gelb, Grün,
// Hellblau, Blau, Lila, Rosa, Grau, Braun, Gold, Silber

const val A_BACK = 0
const val A_ADD = 1
const val A_DELETE = 2
const val A_BIGGER = 3
const val A_SMALLER = 4
const val A_ROT_L = 5
const val A_ROT_R = 6
const val A_UP = 7
const val A_DOWN = 8
const val A_LEFT = 9
const val A_RIGHT = 10
const val A_THICK = 11
const val A_FORMAT = 12
const val A_LAYOUT = 13
const val A_VERT = 14
const val A_HORIZ = 15
const val A_COLOR0 = 16
const val A_COUNT = 30

// Sprachen, die dieselben Wörter wie eine andere Zeile nutzen
private val ALIAS = mapOf("iw" to "he", "ji" to "yi", "ary" to "ar", "arz" to "ar")

private val parsed = HashMap<String, List<String>>()

private fun rowOf(key: String): List<String> = synchronized(parsed) {
    parsed.getOrPut(key) {
        val en = A11Y.getValue("en").split("|")
        val p = (A11Y[key] ?: A11Y.getValue("en")).split("|")
        List(A_COUNT) { i -> p.getOrElse(i) { "" }.ifBlank { en.getOrElse(i) { "" } } }
    }
}

/** Beschreibungstext für TalkBack in der aktuellen Sprache (fehlende Felder: Englisch). */
fun ta(i: Int): String {
    val c = Lang.code
    return rowOf(ALIAS[c] ?: c)[i]
}

val A11Y: Map<String, String> = mapOf(
    "en" to "Back|Add images|Delete|Larger|Smaller|Rotate left|Rotate right|Move up|Move down|Move left|Move right|Thickness|Format|Layout|Vertical|Horizontal|Black|White|Red|Orange|Yellow|Green|Light blue|Blue|Purple|Pink|Gray|Brown|Gold|Silver",
    "de" to "Zurück|Bilder hinzufügen|Löschen|Größer|Kleiner|Nach links drehen|Nach rechts drehen|Nach oben verschieben|Nach unten verschieben|Nach links verschieben|Nach rechts verschieben|Dicke|Format|Raster|Vertikal|Horizontal|Schwarz|Weiß|Rot|Orange|Gelb|Grün|Hellblau|Blau|Lila|Rosa|Grau|Braun|Gold|Silber",
    // ENDE-DER-DATEN
)
