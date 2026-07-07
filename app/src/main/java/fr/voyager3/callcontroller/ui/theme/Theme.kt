package fr.voyager3.callcontroller.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SchemaClair = lightColorScheme(
    primary = VertPrimaryLight,
    onPrimary = VertOnPrimaryLight,
    primaryContainer = VertPrimaryContainerLight,
    onPrimaryContainer = VertOnPrimaryContainerLight,
    secondary = VertSecondaryLight,
    secondaryContainer = VertSecondaryContainerLight,
    tertiary = VertTertiaryLight,
    background = SurfaceLight,
    surface = SurfaceLight,
)

private val SchemaSombre = darkColorScheme(
    primary = VertPrimaryDark,
    onPrimary = VertOnPrimaryDark,
    primaryContainer = VertPrimaryContainerDark,
    onPrimaryContainer = VertOnPrimaryContainerDark,
    secondary = VertSecondaryDark,
    secondaryContainer = VertSecondaryContainerDark,
    tertiary = VertTertiaryDark,
    background = SurfaceDark,
    surface = SurfaceDark,
)

@Composable
fun CallControllerTheme(
    sombre: Boolean = isSystemInDarkTheme(),
    couleurDynamique: Boolean = true,
    content: @Composable () -> Unit,
) {
    val schema = when {
        couleurDynamique && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val contexte = LocalContext.current
            if (sombre) dynamicDarkColorScheme(contexte) else dynamicLightColorScheme(contexte)
        }

        sombre -> SchemaSombre
        else -> SchemaClair
    }

    MaterialTheme(
        colorScheme = schema,
        typography = Typography(),
        shapes = Formes,
        content = content,
    )
}
