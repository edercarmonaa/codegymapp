package mx.com.karedit.codegymapp.data.remote.interceptor

import mx.com.karedit.codegymapp.core.session.SessionExpiredReason
import mx.com.karedit.codegymapp.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class UnauthorizedInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            sessionManager.expireAccessToken(SessionExpiredReason.Unauthorized)
        }
        return response
    }
}
