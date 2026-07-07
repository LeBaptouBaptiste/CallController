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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.voyager3.callcontroller.ui.AppCallController
import fr.voyager3.callcontroller.ui.AppViewModel
import fr.voyager3.callcontroller.ui.theme.CallControllerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as CallControllerApp).container
        val roleManager = getSystemService(RoleManager::class.java)

        setContent {
            CallControllerTheme {
                val vm: AppViewModel = viewModel(factory = AppViewModel.factory(container))
                var roleAccorde by remember { mutableStateOf(roleEstAccorde(roleManager)) }

                val lanceur = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult(),
                ) {
                    roleAccorde = roleEstAccorde(roleManager)
                }

                AppCallController(
                    viewModel = vm,
                    roleAccorde = roleAccorde,
                    onDemanderRole = {
                        val intent = roleManager
                            ?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                        if (intent != null) lanceur.launch(intent)
                    },
                )
            }
        }
    }

    private fun roleEstAccorde(roleManager: RoleManager?): Boolean =
        roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
}
