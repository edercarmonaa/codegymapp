package mx.com.karedit.codegymapp.data.security

class SecureStorageUnavailableException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause)
