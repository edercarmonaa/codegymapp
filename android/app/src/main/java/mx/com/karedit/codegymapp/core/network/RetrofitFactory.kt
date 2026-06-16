package mx.com.karedit.codegymapp.core.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mx.com.karedit.codegymapp.BuildConfig
import mx.com.karedit.codegymapp.core.session.SessionManager
import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.interceptor.AuthHeaderInterceptor
import mx.com.karedit.codegymapp.data.remote.interceptor.HttpsOnlyInterceptor
import mx.com.karedit.codegymapp.data.remote.interceptor.UnauthorizedInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitFactory {
    fun createApi(sessionManager: SessionManager): CodeGymApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(HttpsOnlyInterceptor())
            .addInterceptor(AuthHeaderInterceptor(sessionManager))
            .addInterceptor(UnauthorizedInterceptor(sessionManager))
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CodeGymApi::class.java)
    }
}
