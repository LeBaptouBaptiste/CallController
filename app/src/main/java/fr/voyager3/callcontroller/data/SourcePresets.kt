package fr.voyager3.callcontroller.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Téléchargement des presets/catalogue depuis GitHub. HTTPS uniquement, timeouts
 * courts et taille de réponse bornée — tout ce qui vient du réseau est hostile
 * jusqu'à validation.
 */
class SourcePresets {

    suspend fun telecharger(url: String): String = withContext(Dispatchers.IO) {
        require(url.startsWith("https://")) { "URL non HTTPS refusée" }

        val connexion = (URL(url).openConnection() as HttpsURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            requestMethod = "GET"
        }
        try {
            if (connexion.responseCode != HttpsURLConnection.HTTP_OK) {
                error("HTTP ${connexion.responseCode}")
            }
            lireBorne(connexion)
        } finally {
            connexion.disconnect()
        }
    }

    private fun lireBorne(connexion: HttpsURLConnection): String =
        connexion.inputStream.bufferedReader().use { lecteur ->
            val builder = StringBuilder()
            val tampon = CharArray(TAILLE_TAMPON)
            var lus = lecteur.read(tampon)
            while (lus != -1) {
                builder.append(tampon, 0, lus)
                if (builder.length > TAILLE_MAX) error("Réponse trop volumineuse")
                lus = lecteur.read(tampon)
            }
            builder.toString()
        }

    private companion object {
        const val TIMEOUT_MS = 10_000
        const val TAILLE_TAMPON = 8_192
        const val TAILLE_MAX = 1_000_000
    }
}
