package com.cableplan.viewer.ui.screen

import androidx.compose.ui.graphics.ImageBitmap
import com.cableplan.viewer.domain.PdfItem

data class ViewerState(
    val isLoading: Boolean = false,
    val workspaceLoaded: Boolean = false,
    val pdfs: List<PdfItem> = emptyList(),
    val selectedPdf: PdfItem? = null,
    val imageBitmap: ImageBitmap? = null,
    val pageIndex: Int = 0,
    val pageCount: Int = 0,
    val error: String? = null
)
