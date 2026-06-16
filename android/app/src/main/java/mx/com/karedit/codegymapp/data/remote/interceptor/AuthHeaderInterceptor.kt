package mx.com.karedit.codegymapp.data.remote.interceptor

import mx.com.karedit.codegymapp.BuildConfig
import mx.com.karedit.codegymapp.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .header("User-Agent", BuildConfig.APP_USER_AGENT)

        sessionManager.token()?.takeIf { it.isNotBlank() }?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
