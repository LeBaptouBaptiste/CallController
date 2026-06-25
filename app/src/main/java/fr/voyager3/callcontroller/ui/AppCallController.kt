package fr.voyager3.callcontroller.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import fr.voyager3.callcontroller.matching.ActionRegle

/** Écran racine : navigation par onglets et répartition de l'état vers chaque écran. */
@Composable
fun AppCallController(
    viewModel: AppViewModel,
    roleAccorde: Boolean,
    onDemanderRole: () -> Unit,
) {
    var destination by remember { mutableStateOf(Destination.ACCUEIL) }
    val regles by viewModel.regles.collectAsState(initial = emptyList())
    val journal by viewModel.journal.collectAsState(initial = emptyList())

    val reglesBlocage = regles.filter { it.action == ActionRegle.BLOQUER }
    val listeBlanche = regles.filter { it.action == ActionRegle.AUTORISER }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Destination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = destination == dest,
                        onClick = { destination = dest },
                        icon = { Icon(iconePour(dest), contentDescription = dest.titre) },
                        label = { Text(dest.titre) },
                    )
                }
            }
        },
    ) { contentPadding ->
        Box(Modifier.fillMaxSize().padding(contentPadding)) {
            when (destination) {
                Destination.ACCUEIL -> EcranAccueil(
                    roleAccorde = roleAccorde,
                    nombreReglesBlocage = reglesBlocage.count { it.actif },
                    nombreListeBlanche = listeBlanche.count { it.actif },
                    nombreAppelsBloques = journal.size,
                    onTester = viewModel::tester,
                    onDemanderRole = onDemanderRole,
                )

                Destination.REGLES -> EcranRegles(
                    titre = "Règles de blocage",
                    sousTitre = "Numéros rejetés automatiquement",
                    regles = reglesBlocage,
                    action = ActionRegle.BLOQUER,
                    onAjouter = viewModel::ajouter,
                    onBasculer = viewModel::basculer,
                    onSupprimer = viewModel::supprimer,
                )

                Destination.LISTE_BLANCHE -> EcranRegles(
                    titre = "Liste blanche",
                    sousTitre = "Toujours autorisés, même si une règle correspond",
                    regles = listeBlanche,
                    action = ActionRegle.AUTORISER,
                    onAjouter = viewModel::ajouter,
                    onBasculer = viewModel::basculer,
                    onSupprimer = viewModel::supprimer,
                )

                Destination.JOURNAL -> EcranJournal(
                    appels = journal,
                    onVider = viewModel::viderJournal,
                )
            }
        }
    }
}

private fun iconePour(destination: Destination): ImageVector = when (destination) {
    Destination.ACCUEIL -> Icons.Filled.Home
    Destination.REGLES -> Icons.Filled.Phone
    Destination.LISTE_BLANCHE -> Icons.Filled.CheckCircle
    Destination.JOURNAL -> Icons.Filled.Notifications
}
