package com.cableplan.viewer.domain

data class PdfItem(
    val name: String,
    val absolutePath: String,
    val relativePath: String
)

data class RenderedPdfPage(
    val pageIndex: Int,
    val pageCount: Int,
    val width: Int,
    val height: Int,
    val bytes: ByteArray
)
