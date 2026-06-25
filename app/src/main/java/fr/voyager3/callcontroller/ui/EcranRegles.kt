package fr.voyager3.callcontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.voyager3.callcontroller.matching.ActionRegle
import fr.voyager3.callcontroller.matching.Regle
import fr.voyager3.callcontroller.matching.TypeRegle

/**
 * Écran générique de gestion de règles, réutilisé pour le blocage et la liste
 * blanche (mêmes mécaniques, seule l'[action] diffère).
 */
@Composable
fun EcranRegles(
    titre: String,
    sousTitre: String,
    regles: List<Regle>,
    action: ActionRegle,
    onAjouter: (String, TypeRegle, ActionRegle) -> Unit,
    onBasculer: (Regle) -> Unit,
    onSupprimer: (Regle) -> Unit,
) {
    var dialogueOuvert by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(titre, style = MaterialTheme.typography.headlineSmall)
        Text(sousTitre, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))

        Button(onClick = { dialogueOuvert = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ajouter")
        }
        Spacer(Modifier.height(12.dp))

        if (regles.isEmpty()) {
            Text(
                text = "Aucune entrée pour l'instant.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(regles, key = { it.id }) { regle ->
                    LigneRegle(
                        regle = regle,
                        onBasculer = { onBasculer(regle) },
                        onSupprimer = { onSupprimer(regle) },
                    )
                }
            }
        }
    }

    if (dialogueOuvert) {
        DialogueAjoutRegle(
            action = action,
            onConfirmer = { valeur, type ->
                onAjouter(valeur, type, action)
                dialogueOuvert = false
            },
            onAnnuler = { dialogueOuvert = false },
        )
    }
}

@Composable
private fun LigneRegle(
    regle: Regle,
    onBasculer: () -> Unit,
    onSupprimer: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(regle.valeur, style = MaterialTheme.typography.titleMedium)
                Text(libelleSousTitre(regle), style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = regle.actif, onCheckedChange = { onBasculer() })
            IconButton(onClick = onSupprimer) {
                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
            }
        }
    }
}

private fun libelleSousTitre(regle: Regle): String {
    val type = if (regle.type == TypeRegle.PREFIXE) "Préfixe" else "Expression régulière"
    return if (regle.presetId != null) "$type · preset ${regle.presetId}" else type
}
