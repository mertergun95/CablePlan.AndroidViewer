package com.cableplan.viewer.domain

interface PdfRenderRepository {
    suspend fun open(pdf: PdfItem)
    suspend fun close()
    suspend fun render(pageIndex: Int, maxDimensionPx: Int): RenderedPdfPage
}
