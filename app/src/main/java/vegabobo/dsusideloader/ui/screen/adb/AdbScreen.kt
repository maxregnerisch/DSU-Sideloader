package vegabobo.dsusideloader.ui.screen.adb

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.cards.ContentCopyableCard
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.Dialog
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.screen.Destinations
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

@Composable
fun AdbScreen(
    navController: NavController,
    adbViewModel: AdbViewModel = hiltViewModel()
) {
    val uiState by adbViewModel.uiState.collectAsStateWithLifecycle()
    val scriptPath = adbViewModel.obtainScriptPath()

    val startInstallationCommand = "sh $scriptPath"
    val startInstallationCommandAdb = "adb shell $startInstallationCommand"

    if (uiState.isShowingExitDialog)
        Dialog(
            title = stringResource(id = R.string.return_warning),
            icon = Icons.Outlined.ExitToApp,
            text = stringResource(id = R.string.return_warning_description),
            confirmText = stringResource(id = R.string.yes),
            cancelText = stringResource(id = R.string.no),
            onClickConfirm = { navController.navigateUp() },
            onClickCancel = { adbViewModel.onClickCancelDialog() }
        )

    ApplicationScreen(
        modifier = Modifier.padding(start = 18.dp, end = 18.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.installation),
                scrollBehavior = it,
                showBackButton = true,
                onClickIcon = { navController.navigate(Destinations.Preferences) },
                onClickBackButton = { adbViewModel.onBackPressed() }
            )
        },
        content = {
            Text(text = stringResource(id = R.string.adb_how_to_adb_shell))
            ContentCopyableCard(text = startInstallationCommandAdb)
            Text(text = stringResource(id = R.string.adb_how_to_shell))
            ContentCopyableCard(text = startInstallationCommand)
            Text(text = stringResource(id = R.string.adb_how_to_done))
        }
    )

    BackHandler {
        if (!uiState.isShowingExitDialog)
            adbViewModel.onBackPressed()
    }
}