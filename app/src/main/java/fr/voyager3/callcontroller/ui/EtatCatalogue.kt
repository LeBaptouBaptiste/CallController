package fr.voyager3.callcontroller.ui

import fr.voyager3.callcontroller.data.EntreeCatalogue

/** État de chargement du catalogue de presets communautaires. */
sealed interface EtatCatalogue {
    data object Chargement : EtatCatalogue
    data class Erreur(val message: String) : EtatCatalogue
    data class Charge(val entrees: List<EntreeCatalogue>) : EtatCatalogue
}
