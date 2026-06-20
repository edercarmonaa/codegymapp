package mx.com.karedit.codegymapp.ui.screens.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.domain.model.MobileSummary
import mx.com.karedit.codegymapp.ui.navigation.AppRoutes
import mx.com.karedit.codegymapp.ui.navigation.CodeGymSectionScaffold

@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        viewModel.snackbarShown()
    }

    CodeGymSectionScaffold(
        onBackHome = { onNavigate(AppRoutes.Home) },
        snackbarHostState = snackbarHostState,
        collapsedTitle = "Resumen",
        collapsedSubtitle = "Mes actual",
        isCollapsed = scrollState.value > 96,
        showFab = false
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Resumen", style = MaterialTheme.typography.displayMedium)
            Text(
                text = "Mes actual",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                state.isLoading -> CircularProgressIndicator()
                state.summary == null -> Text("No hay datos de resumen.", style = MaterialTheme.typography.bodyMedium)
                else -> SummaryContent(summary = state.summary!!)
            }
        }
    }
}

@Composable
private fun SummaryContent(summary: MobileSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        MetricGroup(
            title = "Datos generales",
            rows = listOf(
                "Retos cumplidos" to summary.completedMonth.toString(),
                "Cumplimiento general" to summary.generalPercent.asPercent(),
                "Cumplimiento puntual" to summary.onTimePercent.asPercent(),
                "Tiempo practicado" to "${summary.timeMonth} min",
                "Racha actual" to summary.currentStreak.asDays(),
                "Mejor racha" to summary.bestStreak.asDays(),
                "Racha del mes" to summary.monthStreak.asDays(),
                "Retos vencidos" to summary.expiredReview.toString()
            )
        )
        MetricGroup(
            title = "Necesita atención",
            rows = listOf(
                "Retos de hoy" to summary.pendingToday.toString(),
                "Próximos 7 días" to summary.pendingWeek.toString(),
                "Vencidos por revisar" to summary.expiredReview.toString()
            )
        )
    }
}

@Composable
private fun MetricGroup(
    title: String,
    rows: List<Pair<String, String>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        rows.forEach { (label, value) ->
            MetricRow(label = label, value = value)
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun Double.asPercent(): String =
    if (this % 1.0 == 0.0) {
        "${toInt()}%"
    } else {
        "${String.format("%.1f", this)}%"
    }

private fun Int.asDays(): String =
    if (this == 1) {
        "1 día"
    } else {
        "$this días"
    }
