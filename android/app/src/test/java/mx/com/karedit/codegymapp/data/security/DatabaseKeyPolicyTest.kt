package mx.com.karedit.codegymapp.data.security

import org.junit.Assert.assertThrows
import org.junit.Test

class DatabaseKeyPolicyTest {
    @Test
    fun `permite crear clave cuando aun no existe base local`() {
        DatabaseKeyPolicy.requireSafeToCreate(databaseExists = false)
    }

    @Test
    fun `impide regenerar clave si la base cifrada ya existe`() {
        assertThrows(SecureStorageUnavailableException::class.java) {
            DatabaseKeyPolicy.requireSafeToCreate(databaseExists = true)
        }
    }
}
