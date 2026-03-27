package com.cableplan.viewer.domain

import android.net.Uri

interface WorkspaceRepository {
    suspend fun importWorkspace(zipUri: Uri): Result<List<PdfItem>>
    suspend fun listPdfs(): List<PdfItem>
    suspend fun getPdfByRelativePath(relativePath: String): PdfItem?
}
