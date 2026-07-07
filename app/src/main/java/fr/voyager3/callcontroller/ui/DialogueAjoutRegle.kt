@file:OptIn(ExperimentalMaterial3Api::class)

package fr.voyager3.callcontroller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.voyager3.callcontroller.matching.ActionRegle
import fr.voyager3.callcontroller.matching.TypeRegle

/** Dialogue d'ajout d'une règle : sélection préfixe/regex puis saisie de la valeur. */
@Composable
fun DialogueAjoutRegle(
    action: ActionRegle,
    onConfirmer: (String, TypeRegle) -> Unit,
    onAnnuler: () -> Unit,
) {
    var valeur by remember { mutableStateOf("") }
    var modeRegex by remember { mutableStateOf(false) }

    val titre = if (action == ActionRegle.BLOQUER) {
        "Nouvelle règle de blocage"
    } else {
        "Nouvelle entrée en liste blanche"
    }

    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text(titre) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !modeRegex,
                        onClick = { modeRegex = false },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) { Text("Préfixe") }
                    SegmentedButton(
                        selected = modeRegex,
                        onClick = { modeRegex = true },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) { Text("Regex") }
                }
                OutlinedTextField(
                    value = valeur,
                    onValueChange = { valeur = it },
                    singleLine = true,
                    label = {
                        Text(if (modeRegex) "Expression régulière" else "Préfixe (ex. 0162)")
                    },
                    keyboardOptions = if (modeRegex) {
                        KeyboardOptions.Default
                    } else {
                        KeyboardOptions(keyboardType = KeyboardType.Phone)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmer(valeur, if (modeRegex) TypeRegle.REGEX else TypeRegle.PREFIXE)
                },
                enabled = valeur.isNotBlank(),
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text("Annuler") }
        },
    )
}
