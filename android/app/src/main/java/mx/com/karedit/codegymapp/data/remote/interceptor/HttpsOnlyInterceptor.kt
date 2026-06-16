package mx.com.karedit.codegymapp.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class HttpsOnlyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        require(request.url.isHttps) { "Solo se permiten peticiones HTTPS." }
        return chain.proceed(request)
    }
}
