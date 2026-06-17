package mx.com.karedit.codegymapp.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeGymDrawerScaffold(
    title: String,
    selectedRoute: String,
    onNavigate: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    todayCount: Int? = null,
    plannedCount: Int? = null,
    challengesCount: Int? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("CodeGymApp", style = MaterialTheme.typography.headlineSmall)
                    Text("Challenge tracking", style = MaterialTheme.typography.bodyMedium)
                }
                DrawerItem(
                    label = "Mi día",
                    route = AppRoutes.Today,
                    selectedRoute = selectedRoute,
                    count = todayCount,
                    onNavigate = onNavigate,
                    closeDrawer = { scope.launch { drawerState.close() } }
                )
                DrawerItem(
                    label = "Planeado",
                    route = AppRoutes.Planned,
                    selectedRoute = selectedRoute,
                    count = plannedCount,
                    onNavigate = onNavigate,
                    closeDrawer = { scope.launch { drawerState.close() } }
                )
                DrawerItem(
                    label = "Retos",
                    route = AppRoutes.Challenges,
                    selectedRoute = selectedRoute,
                    count = challengesCount,
                    onNavigate = onNavigate,
                    closeDrawer = { scope.launch { drawerState.close() } }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Más secciones",
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        TextButton(onClick = { scope.launch { drawerState.open() } }) {
                            Text("☰")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            content(padding)
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    route: String,
    selectedRoute: String,
    count: Int?,
    onNavigate: (String) -> Unit,
    closeDrawer: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = selectedRoute == route,
        badge = count?.let { { Text(it.toString()) } },
        onClick = {
            closeDrawer()
            onNavigate(route)
        },
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}
