package fr.voyager3.callcontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun EcranJournal(appels: List<AppelBloque>) {
    if (appels.isEmpty()) {
        EtatVide(
            icone = Icons.Filled.Notifications,
            message = "Aucun appel bloqué pour l'instant.",
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(appels, key = { it.id }) { appel -> LigneAppel(appel) }
    }
}

@Composable
private fun LigneAppel(appel: AppelBloque) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(appel.numero, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = formaterDate(appel.horodatage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (appel.motif != null) {
                    Text(
                        text = "Motif : ${appel.motif}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun formaterDate(epochMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date(epochMillis))
