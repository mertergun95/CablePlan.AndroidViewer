package com.cableplan.viewer.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cableplan.viewer.domain.PreferencesRepository
import com.cableplan.viewer.domain.ViewerPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.viewerDataStore by preferencesDataStore(name = "viewer_settings")

@Singleton
class DataStorePreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    override val prefs: Flow<ViewerPreferences> = context.viewerDataStore.data.map { pref ->
        ViewerPreferences(
            workspaceExtractPath = pref[KEY_WORKSPACE_PATH],
            lastPdfRelativePath = pref[KEY_LAST_PDF],
            lastPage = pref[KEY_LAST_PAGE] ?: 0
        )
    }

    override suspend fun saveWorkspace(path: String) {
        context.viewerDataStore.edit { prefs ->
            prefs[KEY_WORKSPACE_PATH] = path
        }
    }

    override suspend fun saveLastPdf(relativePath: String) {
        context.viewerDataStore.edit { prefs ->
            prefs[KEY_LAST_PDF] = relativePath
            prefs[KEY_LAST_PAGE] = 0
        }
    }

    override suspend fun saveLastPage(pageIndex: Int) {
        context.viewerDataStore.edit { prefs ->
            prefs[KEY_LAST_PAGE] = pageIndex
        }
    }

    private companion object {
        val KEY_WORKSPACE_PATH = stringPreferencesKey("workspace_path")
        val KEY_LAST_PDF = stringPreferencesKey("last_pdf")
        val KEY_LAST_PAGE = intPreferencesKey("last_page")
    }
}
