package mx.com.karedit.codegymapp.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import mx.com.karedit.codegymapp.domain.model.MobileChallenge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(viewModel: TodayViewModel) {
    val state by viewModel.state.collectAsState()
    var todayExpanded by remember { mutableStateOf(true) }
    var expiredExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi día") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Hola${state.user?.name?.let { ", $it" } ?: ""}",
                style = MaterialTheme.typography.headlineSmall
            )

            if (state.error != null) {
                Text(
                    text = state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (state.isLoading) {
                TodayLoading()
            } else {
                ChallengeSection(
                    title = "Pendientes de hoy",
                    count = state.todayChallenges.size,
                    expanded = todayExpanded,
                    emptyText = "Sin retos pendientes para hoy.",
                    challenges = state.todayChallenges,
                    onToggle = { todayExpanded = !todayExpanded }
                )
                ChallengeSection(
                    title = "Vencidos pendientes",
                    count = state.expiredChallenges.size,
                    expanded = expiredExpanded,
                    emptyText = "Sin retos vencidos por revisar.",
                    challenges = state.expiredChallenges,
                    onToggle = { expiredExpanded = !expiredExpanded }
                )
            }
        }
    }
}

@Composable
private fun TodayLoading() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator()
        repeat(3) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LoadingLine(widthFraction = 0.45f)
                    LoadingLine(widthFraction = 0.75f)
                }
            }
        }
    }
}

@Composable
private fun LoadingLine(widthFraction: Float) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(14.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun ChallengeSection(
    title: String,
    count: Int,
    expanded: Boolean,
    emptyText: String,
    challenges: List<MobileChallenge>,
    onToggle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text("$count reto${if (count == 1) "" else "s"}", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = onToggle) {
                    Text(if (expanded) "Ocultar" else "Ver")
                }
            }

            if (expanded) {
                if (challenges.isEmpty()) {
                    Text(emptyText, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        challenges.forEach { challenge ->
                            ChallengeCard(challenge = challenge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(challenge: MobileChallenge) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(challenge.platformName, style = MaterialTheme.typography.titleSmall)
            Text(challenge.scheduledDate, style = MaterialTheme.typography.bodySmall)
        }
    }
}
