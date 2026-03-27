package com.cableplan.viewer.ui.screen

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cableplan.viewer.domain.PdfItem
import com.cableplan.viewer.domain.PdfRenderRepository
import com.cableplan.viewer.domain.PreferencesRepository
import com.cableplan.viewer.domain.WorkspaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val workspaceRepository: WorkspaceRepository,
    private val pdfRenderRepository: PdfRenderRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ViewerState())
    val state: StateFlow<ViewerState> = _state.asStateFlow()

    init {
        restoreLastSession()
    }

    fun importWorkspace(zipUri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            workspaceRepository.importWorkspace(zipUri)
                .onSuccess { pdfs ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            workspaceLoaded = true,
                            pdfs = pdfs,
                            selectedPdf = null,
                            imageBitmap = null,
                            pageCount = 0,
                            pageIndex = 0
                        )
                    }
                    preferencesRepository.saveWorkspace("cache/workspace")
                    if (pdfs.isNotEmpty()) {
                        selectPdf(pdfs.first())
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "ZIP load failed") }
                }
        }
    }

    fun selectPdf(pdf: PdfItem) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, selectedPdf = pdf) }
            runCatching {
                pdfRenderRepository.open(pdf)
                renderPage(page = 0)
                preferencesRepository.saveLastPdf(pdf.relativePath)
            }.onFailure { throwable ->
                _state.update { it.copy(isLoading = false, error = throwable.message ?: "PDF open failed") }
            }
        }
    }

    fun nextPage() {
        val nextPage = (_state.value.pageIndex + 1).coerceAtMost((_state.value.pageCount - 1).coerceAtLeast(0))
        if (nextPage != _state.value.pageIndex) {
            viewModelScope.launch { renderPage(nextPage) }
        }
    }

    fun previousPage() {
        val prevPage = (_state.value.pageIndex - 1).coerceAtLeast(0)
        if (prevPage != _state.value.pageIndex) {
            viewModelScope.launch { renderPage(prevPage) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            pdfRenderRepository.close()
        }
    }

    private suspend fun renderPage(page: Int) {
        _state.update { it.copy(isLoading = true) }
        val rendered = pdfRenderRepository.render(pageIndex = page, maxDimensionPx = 2200)
        val image = decodeImage(rendered.bytes)
        _state.update {
            it.copy(
                isLoading = false,
                imageBitmap = image,
                pageIndex = rendered.pageIndex,
                pageCount = rendered.pageCount,
                workspaceLoaded = true
            )
        }
        preferencesRepository.saveLastPage(rendered.pageIndex)
    }

    private fun restoreLastSession() {
        viewModelScope.launch {
            val prefs = preferencesRepository.prefs.first()
            val pdfs = workspaceRepository.listPdfs()
            if (pdfs.isEmpty()) {
                _state.update { it.copy(workspaceLoaded = false, pdfs = emptyList()) }
                return@launch
            }
            _state.update { it.copy(workspaceLoaded = true, pdfs = pdfs) }

            val selected = prefs.lastPdfRelativePath?.let { workspaceRepository.getPdfByRelativePath(it) } ?: pdfs.firstOrNull()
            if (selected != null) {
                _state.update { it.copy(selectedPdf = selected) }
                runCatching {
                    pdfRenderRepository.open(selected)
                    renderPage(prefs.lastPage)
                }.onFailure {
                    renderPage(0)
                }
            }
        }
    }

    private fun decodeImage(bytes: ByteArray): ImageBitmap {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return bitmap.asImageBitmap()
    }
}
