package mx.com.karedit.codegymapp.data.security

interface TokenStorage {
    fun getToken(): String?
    fun getRefreshToken(): String?
    fun saveToken(token: String)
    fun saveRefreshToken(token: String)
    fun clearToken()
    fun clearRefreshToken()
}
