package com.cableplan.viewer

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cableplan.viewer.ui.screen.ViewerScreen
import com.cableplan.viewer.ui.screen.ViewerViewModel
import com.cableplan.viewer.ui.theme.CablePlanTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<ViewerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CablePlanTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val zipPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->
                    if (uri != null) {
                        viewModel.importWorkspace(uri)
                    }
                }

                val onPickZip = remember {
                    { zipPickerLauncher.launch(arrayOf("application/zip", "application/octet-stream")) }
                }

                ViewerScreen(
                    state = state,
                    onOpenZipClicked = onPickZip,
                    onPdfSelected = viewModel::selectPdf,
                    onNextPage = viewModel::nextPage,
                    onPrevPage = viewModel::previousPage
                )
            }
        }
    }
}
