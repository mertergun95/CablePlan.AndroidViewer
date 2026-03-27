package com.cableplan.viewer.data

import android.content.Context
import android.net.Uri
import com.cableplan.viewer.domain.PdfItem
import com.cableplan.viewer.domain.WorkspaceRepository
import com.cableplan.viewer.util.ZipExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZipWorkspaceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val zipExtractor: ZipExtractor
) : WorkspaceRepository {

    private val workspaceRoot = File(context.cacheDir, "workspace")

    override suspend fun importWorkspace(zipUri: Uri): Result<List<PdfItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val zipFile = File(context.cacheDir, "workspace.zip")
            context.contentResolver.openInputStream(zipUri)?.use { input ->
                zipFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: error("ZIP file could not be opened")

            if (workspaceRoot.exists()) {
                workspaceRoot.deleteRecursively()
            }
            workspaceRoot.mkdirs()

            zipExtractor.extract(zipFile, workspaceRoot)
            listPdfs()
        }
    }

    override suspend fun listPdfs(): List<PdfItem> = withContext(Dispatchers.IO) {
        val pdfDir = File(workspaceRoot, "pdf")
        if (!pdfDir.exists()) return@withContext emptyList()

        pdfDir.walkTopDown()
            .filter { it.isFile && it.extension.equals("pdf", ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
            .map {
                PdfItem(
                    name = it.name,
                    absolutePath = it.absolutePath,
                    relativePath = it.relativeTo(workspaceRoot).path
                )
            }
            .toList()
    }

    override suspend fun getPdfByRelativePath(relativePath: String): PdfItem? = withContext(Dispatchers.IO) {
        val file = File(workspaceRoot, relativePath)
        if (!file.exists()) return@withContext null
        PdfItem(
            name = file.name,
            absolutePath = file.absolutePath,
            relativePath = relativePath
        )
    }
}
