package fr.voyager3.callcontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.voyager3.callcontroller.data.EntreeCatalogue

/** Catalogue de presets communautaires : abonnement, mise à jour, désabonnement. */
@Composable
fun EcranPresets(
    etat: EtatCatalogue,
    abonnements: Set<String>,
    erreurAction: String?,
    onSabonner: (EntreeCatalogue) -> Unit,
    onSeDesabonner: (String) -> Unit,
    onRafraichir: () -> Unit,
    onEffacerErreur: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        erreurAction?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onEffacerErreur) { Text("OK") }
                }
            }
        }

        when (etat) {
            EtatCatalogue.Chargement -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }

            is EtatCatalogue.Erreur -> EtatErreur(etat.message, onRafraichir)

            is EtatCatalogue.Charge ->
                if (etat.entrees.isEmpty()) {
                    EtatVide(Icons.Filled.Star, "Aucun preset disponible dans le catalogue.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(etat.entrees, key = { it.id }) { entree ->
                            LignePreset(
                                entree = entree,
                                abonne = entree.id in abonnements,
                                onSabonner = onSabonner,
                                onSeDesabonner = onSeDesabonner,
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun EtatErreur(message: String, onRafraichir: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.height(12.dp))
        Text("Catalogue indisponible.", style = MaterialTheme.typography.titleMedium)
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRafraichir) { Text("Réessayer") }
    }
}

@Composable
private fun LignePreset(
    entree: EntreeCatalogue,
    abonne: Boolean,
    onSabonner: (EntreeCatalogue) -> Unit,
    onSeDesabonner: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(entree.nom, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "v${entree.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (abonne) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Abonné",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (entree.description.isNotBlank()) {
                Text(
                    text = entree.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (abonne) {
                    OutlinedButton(onClick = { onSeDesabonner(entree.id) }) { Text("Se désabonner") }
                    TextButton(onClick = { onSabonner(entree) }) { Text("Mettre à jour") }
                } else {
                    Button(onClick = { onSabonner(entree) }) { Text("S'abonner") }
                }
            }
        }
    }
}
