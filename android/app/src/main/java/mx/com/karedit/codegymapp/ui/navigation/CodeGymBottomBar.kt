package mx.com.karedit.codegymapp.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CodeGymBottomBar(
    selectedRoute: String,
    onRouteSelected: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedRoute == AppRoutes.Today,
            onClick = { onRouteSelected(AppRoutes.Today) },
            icon = { Text("D") },
            label = { Text("Mi día") }
        )
        NavigationBarItem(
            selected = selectedRoute == AppRoutes.Planned,
            onClick = { onRouteSelected(AppRoutes.Planned) },
            icon = { Text("P") },
            label = { Text("Planeado") }
        )
        NavigationBarItem(
            selected = selectedRoute == AppRoutes.Challenges,
            onClick = { onRouteSelected(AppRoutes.Challenges) },
            icon = { Text("R") },
            label = { Text("Retos") }
        )
    }
}
