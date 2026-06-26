package fr.voyager3.callcontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.voyager3.callcontroller.data.AppelBloque
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Journal des appels bloqués (du plus récent au plus ancien). */
@Composable
fun EcranJournal(
    appels: List<AppelBloque>,
    onVider: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Appels bloqués",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            if (appels.isNotEmpty()) {
                TextButton(onClick = onVider) { Text("Vider") }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (appels.isEmpty()) {
            Text(
                text = "Aucun appel bloqué pour l'instant.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(appels, key = { it.id }) { appel -> LigneAppel(appel) }
            }
        }
    }
}

@Composable
private fun LigneAppel(appel: AppelBloque) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(appel.numero, style = MaterialTheme.typography.titleMedium)
            Text(formaterDate(appel.horodatage), style = MaterialTheme.typography.bodySmall)
            if (appel.motif != null) {
                Text("Motif : ${appel.motif}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formaterDate(epochMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date(epochMillis))
