package mx.com.karedit.codegymapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
    collapsedTitle: String = "",
    collapsedSubtitle: String? = null,
    isCollapsed: Boolean = false,
    showFab: Boolean = true,
    onFabClick: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isCollapsed) {
                        Column {
                            Text(collapsedTitle, style = MaterialTheme.typography.titleLarge)
                            if (!collapsedSubtitle.isNullOrBlank()) {
                                Text(
                                    collapsedSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBackHome) {
                        Text("‹", style = MaterialTheme.typography.displaySmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
