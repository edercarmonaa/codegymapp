package mx.com.karedit.codegymapp.domain.model

import java.text.Normalizer
import java.util.Locale

data class ChallengeDifficulty(val value: String, val label: String)

// Also used when upgrading an installation that still has legacy offline data.
fun difficultyKey(value: String): String {
    val key = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
    return when (key) {
        "easy" -> "facil"
        "medium", "media", "intermedio", "intermedia" -> "medio"
        "hard" -> "dificil"
        else -> key
    }
}

fun defaultDifficulties(): List<ChallengeDifficulty> = listOf(
    ChallengeDifficulty("facil", "Fácil"),
    ChallengeDifficulty("medio", "Medio"),
    ChallengeDifficulty("dificil", "Difícil")
)
