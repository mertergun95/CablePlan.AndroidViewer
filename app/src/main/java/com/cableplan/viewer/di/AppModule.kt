package com.cableplan.viewer.di

import com.cableplan.viewer.data.AndroidPdfRenderRepository
import com.cableplan.viewer.data.DataStorePreferencesRepository
import com.cableplan.viewer.data.ZipWorkspaceRepository
import com.cableplan.viewer.domain.PdfRenderRepository
import com.cableplan.viewer.domain.PreferencesRepository
import com.cableplan.viewer.domain.WorkspaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindWorkspaceRepository(impl: ZipWorkspaceRepository): WorkspaceRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: DataStorePreferencesRepository): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindPdfRenderRepository(impl: AndroidPdfRenderRepository): PdfRenderRepository
}
