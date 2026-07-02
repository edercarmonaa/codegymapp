package mx.com.karedit.codegymapp.ui.screens.account

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.BuildConfig
import mx.com.karedit.codegymapp.domain.model.User
import mx.com.karedit.codegymapp.ui.components.ListSkeleton
import mx.com.karedit.codegymapp.ui.feedback.rememberCodeGymHapticSnackbar
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymSectionScaffold

@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val showHapticSnackbar = rememberCodeGymHapticSnackbar(snackbarHostState)
    val scrollState = rememberScrollState()

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        showHapticSnackbar(message)
    }

    CodeGymSectionScaffold(
        onBackHome = { onNavigate(AppRoutes.Home) },
        snackbarHostState = snackbarHostState,
        collapsedTitle = "Mi cuenta",
        isCollapsed = scrollState.value > 96,
        showFab = false
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Mi cuenta", style = MaterialTheme.typography.displayMedium)

            when {
                state.isLoading -> ListSkeleton(count = 3)
                state.user != null -> AccountContent(
                    user = state.user!!,
                    onSettings = { onNavigate(AppRoutes.Settings) },
                    onLogout = viewModel::logout
                )
                else -> Text("No se pudo cargar la sesión.", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun AccountContent(
    user: User,
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AccountAvatar(initial = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "C")
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name.ifBlank { user.username }, style = MaterialTheme.typography.headlineSmall)
                Text(user.email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        AccountRow(label = "Usuario", value = user.username)
        AccountRow(label = "Estado de sesión", value = "Activa")
        AccountRow(label = "Versión", value = BuildConfig.VERSION_NAME)

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSettings
        ) {
            Text("Configuración")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onLogout
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun AccountRow(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value.ifBlank { "-" }, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun AccountAvatar(initial: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(color = Color(0xFFEDEDED))
        }
        Text(
            text = initial,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black
        )
    }
}
