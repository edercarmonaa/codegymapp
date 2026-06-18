package mx.com.karedit.codegymapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeGymSectionScaffold(
    onBackHome: () -> Unit,
    snackbarHostState: SnackbarHostState,
    showFab: Boolean = true,
    onFabClick: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = onBackHome) {
                        Text("‹", style = androidx.compose.material3.MaterialTheme.typography.displaySmall)
                    }
                },
                actions = {
                    TextButton(onClick = {}) {
                        Text("⋮", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(onClick = onFabClick) {
                    Text("+", style = androidx.compose.material3.MaterialTheme.typography.displaySmall)
                }
            }
        }
    ) { padding ->
        content(padding)
    }
}
