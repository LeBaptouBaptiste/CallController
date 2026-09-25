package fr.voyager3.callcontroller

import android.app.role.RoleManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.voyager3.callcontroller.ui.AppCallController
import fr.voyager3.callcontroller.ui.AppViewModel
import fr.voyager3.callcontroller.ui.theme.CallControllerTheme

class MainActivity : ComponentActivity() {

    private val roleManager by lazy { getSystemService(RoleManager::class.java) }

    // Relu à chaque reprise : le rôle peut être accordé ou révoqué depuis les réglages système.
    private var roleAccorde by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as CallControllerApp).container

        setContent {
            CallControllerTheme {
                val vm: AppViewModel = viewModel(factory = AppViewModel.factory(container))
                var demandeRoleRefusee by rememberSaveable { mutableStateOf(false) }

                val lanceur = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult(),
                ) {
                    roleAccorde = roleEstAccorde()
                    demandeRoleRefusee = !roleAccorde
                }

                AppCallController(
                    viewModel = vm,
                    roleAccorde = roleAccorde,
                    demandeRoleRefusee = demandeRoleRefusee,
                    onDemanderRole = {
                        val intent = roleManager
                            ?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                        if (intent != null) lanceur.launch(intent)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        roleAccorde = roleEstAccorde()
    }

    private fun roleEstAccorde(): Boolean =
        roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
}
