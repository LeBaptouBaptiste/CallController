package fr.voyager3.callcontroller.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SchemaClair = lightColorScheme(primary = Vert700, secondary = Vert500)
private val SchemaSombre = darkColorScheme(primary = Vert500, secondary = Vert700)

@Composable
fun CallControllerTheme(
    sombre: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (sombre) SchemaSombre else SchemaClair,
        typography = Typography(),
        content = content,
    )
}
