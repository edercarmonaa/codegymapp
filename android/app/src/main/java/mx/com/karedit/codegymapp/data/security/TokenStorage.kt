package mx.com.karedit.codegymapp.data.security

interface TokenStorage {
    fun getToken(): String?
    fun saveToken(token: String)
    fun clearToken()
}
