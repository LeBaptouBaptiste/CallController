package fr.voyager3.callcontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.voyager3.callcontroller.matching.Decision
import fr.voyager3.callcontroller.matching.ResultatEvaluation

/** Écran d'accueil : état du filtrage, statistiques, test d'un numéro, activation du rôle. */
@Composable
fun EcranAccueil(
    roleAccorde: Boolean,
    nombreReglesBlocage: Int,
    nombreListeBlanche: Int,
    nombreAppelsBloques: Int,
    bloquerMasques: Boolean,
    onBloquerMasquesChange: (Boolean) -> Unit,
    onTester: (String) -> ResultatEvaluation,
    onDemanderRole: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CarteStatut(roleAccorde)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LigneStat(Icons.Filled.Phone, "Règles de blocage actives", nombreReglesBlocage)
                LigneStat(Icons.Filled.CheckCircle, "Numéros en liste blanche", nombreListeBlanche)
                LigneStat(Icons.Filled.Notifications, "Appels bloqués (journal)", nombreAppelsBloques)
            }
        }

        CarteReglages(bloquerMasques, onBloquerMasquesChange)

        CarteTest(onTester)

        if (!roleAccorde) {
            Button(onClick = onDemanderRole, modifier = Modifier.fillMaxWidth()) {
                Text("Activer le filtrage des appels")
            }
        }
    }
}

@Composable
private fun CarteStatut(roleAccorde: Boolean) {
    val couleurFond = if (roleAccorde) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val couleurTexte = if (roleAccorde) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = couleurFond),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = if (roleAccorde) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = couleurTexte,
                modifier = Modifier.size(36.dp),
            )
            Column {
                Text(
                    text = if (roleAccorde) "Filtrage actif" else "Filtrage inactif",
                    style = MaterialTheme.typography.titleMedium,
                    color = couleurTexte,
                )
                Text(
                    text = if (roleAccorde) {
                        "Les appels indésirables sont rejetés automatiquement."
                    } else {
                        "Active le rôle système pour filtrer les appels."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = couleurTexte,
                )
            }
        }
    }
}

@Composable
private fun LigneStat(icone: ImageVector, libelle: String, valeur: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(libelle, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(valeur.toString(), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CarteReglages(bloquerMasques: Boolean, onChange: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Bloquer les numéros masqués", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Appels sans numéro affiché (privé, inconnu).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = bloquerMasques, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun CarteTest(onTester: (String) -> ResultatEvaluation) {
    var numero by remember { mutableStateOf("") }
    var resultat by remember { mutableStateOf<ResultatEvaluation?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Tester un numéro", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = numero,
                onValueChange = {
                    numero = it
                    resultat = null
                },
                singleLine = true,
                label = { Text("Ex. 0162123456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { resultat = onTester(numero) },
                enabled = numero.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Tester") }

            resultat?.let { r ->
                val bloque = r.decision == Decision.REJETER
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = if (bloque) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (bloque) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = if (bloque) {
                            "Serait bloqué" + (r.motif?.let { " (motif : $it)" } ?: "")
                        } else {
                            "Serait autorisé"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
