package com.lisb.google.devtools.ksp.symbol

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.lisb.google.devtools.ksp.symbol.KSClassDeclarationExt.getAllParentFiles
import com.lisb.google.devtools.ksp.symbol.KSClassDeclarationExt.getFields
import com.lisb.java.io.File.FileExt.toSourceFile
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.configureKsp
import junit.framework.Assert.assertEquals
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import java.io.File

class KSClassDeclarationExtTest {
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun testGetAllParentFiles() {
        val result = kspProcess(
            "src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource1.kt",
            "src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource1Parent.kt",
            "src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource1GrandParent.kt"
        ) { resolver ->
            val name =
                resolver.getKSNameFromString("com.lisb.google.devtools.ksp.symbol.TestSource1")
            val parentFiles = resolver.getClassDeclarationByName(name)!!.getAllParentFiles()
            assertEquals(
                parentFiles.map { it.declarations.first().qualifiedName!!.asString() }
                    .sorted(),
                listOf(
                    "com.lisb.google.devtools.ksp.symbol.TestSource1Parent",
                    "com.lisb.google.devtools.ksp.symbol.TestSource1GrandParent"
                ).sorted()
            )
        }
        assertEquals(result.messages, result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun testGetFieldsWithStaticField() {
        val result = kspProcess(
            "src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource2.java",
            "src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource2Parent.java",
            "src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource2GrandParent.java"
        ) { resolver ->
            val name =
                resolver.getKSNameFromString("com.lisb.google.devtools.ksp.symbol.TestSource2")
            val fields = resolver.getClassDeclarationByName(name)!!
                .getFields(withStaticField = true)
                .toList()
            assertEquals(
                fields.map { it.simpleName.asString() }.sorted(),
                listOf(
                    "PrivateTestSource",
                    "PublicTestSource",
                    "ProtectedTestCaseParent",
                    "PublicTestCaseGrandParent",
                    "STATIC_FIELD",
                    "PROTECTED_PARENT_STATIC_FIELD",
                    "PUBLIC_GRAND_PARENT_STATIC_FIELD"
                ).sorted()
            )
        }
        assertEquals(result.messages, result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun testGetFieldsWithOutStaticField() {
        val result = kspProcess(
            "src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource2.java",
            "src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource2Parent.java",
            "src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource2GrandParent.java"
        ) { resolver ->
            val name =
                resolver.getKSNameFromString("com.lisb.google.devtools.ksp.symbol.TestSource2")
            val fields = resolver.getClassDeclarationByName(name)!!
                .getFields(withStaticField = false)
                .toList()
            assertEquals(
                fields.map { it.simpleName.asString() }.sorted(),
                listOf(
                    "PrivateTestSource",
                    "PublicTestSource",
                    "ProtectedTestCaseParent",
                    "PublicTestCaseGrandParent"
                ).sorted()
            )
        }
        assertEquals(result.messages, result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    /**
     * [sources] をコンパイルした際に実行されるKSPの [SymbolProcessor.process] 内で [block] を実行する
     */
    @OptIn(ExperimentalCompilerApi::class)
    private fun kspProcess(
        vararg sources: String,
        block: (Resolver) -> Unit
    ): JvmCompilationResult {
        return KotlinCompilation().apply {
            configureKsp(useKsp2 = true) {
                symbolProcessorProviders += object : SymbolProcessorProvider {
                    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
                        return object : SymbolProcessor {
                            override fun process(resolver: Resolver): List<KSAnnotated> {
                                block(resolver)
                                return emptyList()
                            }
                        }
                    }
                }
            }
            this.sources = sources.map { File(it).toSourceFile() }
        }.compile()
    }
}