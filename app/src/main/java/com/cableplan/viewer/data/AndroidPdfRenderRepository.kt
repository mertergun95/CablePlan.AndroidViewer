package com.cableplan.viewer.data

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.cableplan.viewer.domain.PdfItem
import com.cableplan.viewer.domain.PdfRenderRepository
import com.cableplan.viewer.domain.RenderedPdfPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidPdfRenderRepository @Inject constructor() : PdfRenderRepository {

    private var descriptor: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null

    override suspend fun open(pdf: PdfItem) = withContext(Dispatchers.IO) {
        close()
        descriptor = ParcelFileDescriptor.open(File(pdf.absolutePath), ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(descriptor!!)
    }

    override suspend fun close() = withContext(Dispatchers.IO) {
        renderer?.close()
        renderer = null
        descriptor?.close()
        descriptor = null
    }

    override suspend fun render(pageIndex: Int, maxDimensionPx: Int): RenderedPdfPage = withContext(Dispatchers.IO) {
        val activeRenderer = renderer ?: error("PDF not opened")
        require(activeRenderer.pageCount > 0) { "PDF has no pages" }

        val clampedIndex = pageIndex.coerceIn(0, activeRenderer.pageCount - 1)
        activeRenderer.openPage(clampedIndex).use { page ->
            val ratio = page.width.toFloat() / page.height.toFloat()
            val width: Int
            val height: Int
            if (ratio >= 1f) {
                width = maxDimensionPx
                height = (maxDimensionPx / ratio).toInt().coerceAtLeast(1)
            } else {
                height = maxDimensionPx
                width = (maxDimensionPx * ratio).toInt().coerceAtLeast(1)
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            val bytes = ByteArrayOutputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.toByteArray()
            }
            bitmap.recycle()

            RenderedPdfPage(
                pageIndex = clampedIndex,
                pageCount = activeRenderer.pageCount,
                width = width,
                height = height,
                bytes = bytes
            )
        }
    }
}
