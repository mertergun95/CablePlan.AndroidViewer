package com.cableplan.viewer.util

import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

class ZipExtractor @Inject constructor() {

    fun extract(zipFile: File, outputDir: File) {
        if (!outputDir.exists()) outputDir.mkdirs()

        ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                val outputFile = File(outputDir, entry.name)
                if (entry.isDirectory) {
                    outputFile.mkdirs()
                } else {
                    outputFile.parentFile?.mkdirs()
                    outputFile.outputStream().use { output ->
                        zipInputStream.copyTo(output)
                    }
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
        }
    }
}
