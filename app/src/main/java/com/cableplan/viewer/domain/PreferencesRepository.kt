package com.cableplan.viewer.domain

import kotlinx.coroutines.flow.Flow

data class ViewerPreferences(
    val workspaceExtractPath: String? = null,
    val lastPdfRelativePath: String? = null,
    val lastPage: Int = 0
)

interface PreferencesRepository {
    val prefs: Flow<ViewerPreferences>
    suspend fun saveWorkspace(path: String)
    suspend fun saveLastPdf(relativePath: String)
    suspend fun saveLastPage(pageIndex: Int)
}
