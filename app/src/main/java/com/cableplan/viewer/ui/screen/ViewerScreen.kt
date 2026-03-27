package com.cableplan.viewer.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ViewerScreen(
    state: ViewerState,
    onOpenZipClicked: () -> Unit,
    onPdfSelected: (pdf: com.cableplan.viewer.domain.PdfItem) -> Unit,
    onNextPage: () -> Unit,
    onPrevPage: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "PDF Files",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn {
                    items(state.pdfs, key = { it.absolutePath }) { pdf ->
                        val isSelected = pdf.absolutePath == state.selectedPdf?.absolutePath
                        Text(
                            text = pdf.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                .clickable {
                                    onPdfSelected(pdf)
                                    scope.launch { drawerState.close() }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 6.dp, end = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Open drawer", tint = Color.White)
                    }

                    IconButton(onClick = onOpenZipClicked) {
                        Icon(Icons.Default.FolderZip, contentDescription = "Open ZIP", tint = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        state.imageBitmap != null -> {
                            Image(
                                bitmap = state.imageBitmap,
                                contentDescription = state.selectedPdf?.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        state.error != null -> {
                            Text(text = state.error, color = Color.White)
                        }

                        !state.workspaceLoaded -> {
                            Text(text = "ZIP seçerek başlayın", color = Color.White)
                        }

                        else -> {
                            Text(text = "PDF seçin", color = Color.White)
                        }
                    }

                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevPage, enabled = state.pageIndex > 0) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = Color.White)
                    }
                    Text(
                        text = if (state.pageCount > 0) "${state.pageIndex + 1}/${state.pageCount}" else "0/0",
                        color = Color.White
                    )
                    IconButton(onClick = onNextPage, enabled = state.pageIndex + 1 < state.pageCount) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = Color.White)
                    }
                }
            }
        }
    }
}
