package com.lisb.java.io.File

import com.tschuchort.compiletesting.SourceFile
import java.io.File

object FileExt {
    fun File.toSourceFile(): SourceFile {
        return SourceFile.new(this.name, this.readText())
    }
}