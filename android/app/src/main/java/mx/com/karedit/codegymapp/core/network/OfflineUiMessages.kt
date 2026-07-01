package mx.com.karedit.codegymapp.core.network

fun Throwable.isOfflineUiMessage(): Boolean {
    val text = message?.trim()?.trimEnd('.') ?: return false
    return text == "Sin conexión a internet"
}
