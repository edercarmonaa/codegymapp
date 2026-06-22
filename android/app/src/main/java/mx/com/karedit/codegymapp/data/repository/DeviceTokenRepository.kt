package mx.com.karedit.codegymapp.data.repository

import mx.com.karedit.codegymapp.data.remote.api.CodeGymApi
import mx.com.karedit.codegymapp.data.remote.dto.MobileDeviceTokenRequestDto

class DeviceTokenRepository(private val api: CodeGymApi) {
    suspend fun store(
        token: String,
        deviceName: String,
        appVersion: String
    ): Result<String> = runCatching {
        val response = api.storeDeviceToken(
            MobileDeviceTokenRequestDto(
                token = token,
                deviceName = deviceName,
                appVersion = appVersion
            )
        )
        if (!response.ok) {
            error(response.message ?: "No se pudo registrar el dispositivo.")
        }

        response.message ?: "Dispositivo registrado."
    }
}
