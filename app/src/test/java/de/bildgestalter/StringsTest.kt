package de.bildgestalter

import java.io.File
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Test

class StringsTest {

    // Bewusst überwiegend Englisch (siehe Erklärung zu Cherokee und Manx)
    private val mostlyEnglishOk = setOf("chr", "gv")

    private fun configTags(): List<String> {
        val text = File("src/main/res/xml/locales_config.xml").readText()
        return Regex("android:name=\"([^\"]+)\"").findAll(text).map { it.groupValues[1] }.toList()
    }

    @Test
    fun alleSprachenSindVollstaendig() {
        val en = RAW_DATA.getValue("en").split("|")
        val problems = ArrayList<String>()
        for ((key, row) in RAW_DATA) {
            val p = row.split("|")
            if (p.size != S_COUNT) {
                problems.add("$key: ${p.size} statt $S_COUNT Feldern")
                continue
            }
            p.forEachIndexed { i, s -> if (s.isBlank()) problems.add("$key: Feld $i ist leer") }
            if (!p[S_SAVED].contains("%s")) problems.add("$key: Feld $S_SAVED enthält kein %s")
            if (key != "en" && key !in mostlyEnglishOk) {
                val same = p.indices.count { p[it] == en[it] }
                if (same > 28) problems.add("$key: $same Felder sind noch Englisch")
            }
        }
        assertTrue("Sprachen unvollständig:\n" + problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun androidFindetJedeSprache() {
        val problems = ArrayList<String>()
        val covered = HashSet<String>()
        for (tag in configTags()) {
            val key = resolveX(Locale.forLanguageTag(tag))
            covered.add(key)
            if (!DATA.containsKey(key)) problems.add("$tag: keine Übersetzung für \"$key\"")
        }
        for (key in DATA.keys) {
            if (key == "iw" || key == "ji") continue
            if (key !in covered) problems.add("$key: fehlt in locales_config.xml")
        }
        val src = File("src/main/java/de/bildgestalter/Strings.kt").readText()
        if (!src.contains("resolveX(")) {
            problems.add("Strings.kt: 'resolve(list.get(i))' muss 'resolveX(list.get(i))' heißen")
        }
        assertTrue("Sprachliste fehlerhaft:\n" + problems.joinToString("\n"), problems.isEmpty())
    }
}
