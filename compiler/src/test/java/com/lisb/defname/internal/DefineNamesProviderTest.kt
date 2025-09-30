package com.lisb.defname.internal

import com.lisb.java.io.File.FileExt.toSourceFile
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.configureKsp
import junit.framework.Assert.assertEquals
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import java.io.File
import kotlin.io.path.Path

class DefineNamesProviderTest {

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `process returns annotated class`() {
        val result = KotlinCompilation().apply {
            configureKsp(useKsp2 = true) {
                symbolProcessorProviders += DefineNamesProvider()
            }
            sources = listOf(
                File("src/test/java/com/lisb/defname/internal/TestSource1.java").toSourceFile(),
                File("src/test/java/com/lisb/defname/internal/TestSource1Parent.java").toSourceFile(),
                File("../annotation/src/main/java/com/lisb/defname/DefineNames.java").toSourceFile(),
                File("../annotation/src/main/java/com/lisb/defname/DefineName.java").toSourceFile(),
            )
        }.compile()
        assertEquals(result.messages, result.exitCode, KotlinCompilation.ExitCode.OK)
        assertSourceEquals(
            File("src/test/resources/com/lisb/defname/internal/Expected_CTestSource1.java"),
            File(
                Path(
                    result.outputDirectory.path,
                    "../ksp/sources/java/com/lisb/defname/internal/_CTestSource1.java"
                ).toString()
            )
        )
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun `process returns annotated class2`() {
        val result = KotlinCompilation().apply {
            configureKsp(useKsp2 = true) {
                symbolProcessorProviders += DefineNamesProvider()
            }
            sources = listOf(
                File("src/test/java/com/lisb/defname/internal/TestSource2.java").toSourceFile(),
                File("../annotation/src/main/java/com/lisb/defname/DefineNames.java").toSourceFile(),
                File("../annotation/src/main/java/com/lisb/defname/DefineName.java").toSourceFile(),
            )
        }.compile()
        assertEquals(result.messages, result.exitCode, KotlinCompilation.ExitCode.OK)
        assertSourceEquals(
            File("src/test/resources/com/lisb/defname/internal/Expected_CTestSource2.java"),
            File(
                Path(
                    result.outputDirectory.path,
                    "../ksp/sources/java/com/lisb/defname/internal/_CTestSource2.java"
                ).toString()
            )
        )
    }

    private fun assertSourceEquals(expectedFile: File, actualFile: File) {
        assertEquals(
            expectedFile.readText().trimIndent(),
            actualFile.readText().trimIndent()
        )
    }
}
