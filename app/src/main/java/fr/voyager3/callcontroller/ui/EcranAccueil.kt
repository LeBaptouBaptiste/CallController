package fr.voyager3.callcontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    onTester: (String) -> ResultatEvaluation,
    onDemanderRole: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("CallController", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Filtrage automatique du démarchage téléphonique",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (roleAccorde) "✅ Filtrage actif" else "⚠️ Filtrage inactif",
                    style = MaterialTheme.typography.titleMedium,
                )
                if (!roleAccorde) {
                    Text(
                        text = "Active le rôle système pour que les appels soient filtrés.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Ligne("Règles de blocage actives", nombreReglesBlocage)
                Ligne("Numéros en liste blanche", nombreListeBlanche)
                Ligne("Appels bloqués (journal)", nombreAppelsBloques)
            }
        }

        CarteTest(onTester)

        if (!roleAccorde) {
            Button(onClick = onDemanderRole, modifier = Modifier.fillMaxWidth()) {
                Text("Activer le filtrage des appels")
            }
        }
    }
}

@Composable
private fun Ligne(libelle: String, valeur: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(libelle, style = MaterialTheme.typography.bodyMedium)
        Text(valeur.toString(), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CarteTest(onTester: (String) -> ResultatEvaluation) {
    var numero by remember { mutableStateOf("") }
    var resultat by remember { mutableStateOf<ResultatEvaluation?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                val texte = if (r.decision == Decision.REJETER) {
                    "🚫 Serait bloqué" + (r.motif?.let { " (motif : $it)" } ?: "")
                } else {
                    "✅ Serait autorisé"
                }
                Text(texte, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
