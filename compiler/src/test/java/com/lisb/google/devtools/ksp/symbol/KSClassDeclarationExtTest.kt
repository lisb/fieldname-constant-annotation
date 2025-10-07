package com.lisb.google.devtools.ksp.symbol

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.lisb.google.devtools.ksp.symbol.KSClassDeclarationExt.getAllParentFiles
import com.lisb.google.devtools.ksp.symbol.KSClassDeclarationExt.getFields
import com.lisb.java.io.File.FileExt.toSourceFile
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
        val result = KotlinCompilation().apply {
            configureKsp(useKsp2 = true) {
                symbolProcessorProviders += TestGetAllParentFilesProvider()
            }
            sources = listOf(
                File("src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource3.kt").toSourceFile(),
                File("src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource3Parent.kt").toSourceFile(),
                File("src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource3GrandParent.kt").toSourceFile(),
            )
        }.compile()
        assertEquals(result.messages, result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    private class TestGetAllParentFilesProvider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return Processor()
        }

        private class Processor() : SymbolProcessor {

            @OptIn(KspExperimental::class)
            override fun process(resolver: Resolver): List<KSAnnotated> {
                val name =
                    resolver.getKSNameFromString("com.lisb.google.devtools.ksp.symbol.TestSource3")
                val parentFiles = resolver.getClassDeclarationByName(name)!!.getAllParentFiles()
                assertEquals(
                    parentFiles.map { it.declarations.first().qualifiedName!!.asString() }
                        .sorted(),
                    listOf(
                        "com.lisb.google.devtools.ksp.symbol.TestSource3Parent",
                        "com.lisb.google.devtools.ksp.symbol.TestSource3GrandParent"
                    ).sorted()
                )
                return emptyList()
            }
        }
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun testGetFieldsWithStaticField() {
        val result = KotlinCompilation().apply {
            configureKsp(useKsp2 = true) {
                symbolProcessorProviders += TestGetFieldsProvider(withStaticField = true) { fieldNames ->
                    assertEquals(
                        fieldNames,
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
            }
            sources = listOf(
                File("src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource4.java").toSourceFile(),
                File("src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource4Parent.java").toSourceFile(),
                File("src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource4GrandParent.java").toSourceFile(),
            )
        }.compile()
        assertEquals(result.messages, result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun testGetFieldsWithOutStaticField() {
        val result = KotlinCompilation().apply {
            configureKsp(useKsp2 = true) {
                symbolProcessorProviders += TestGetFieldsProvider(withStaticField = false) { fieldNames ->
                    assertEquals(
                        fieldNames,
                        listOf(
                            "PrivateTestSource",
                            "PublicTestSource",
                            "ProtectedTestCaseParent",
                            "PublicTestCaseGrandParent",
                        ).sorted()
                    )
                }
            }
            sources = listOf(
                File("src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource4.java").toSourceFile(),
                File("src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource4Parent.java").toSourceFile(),
                File("src/test/java/com/lisb/google/devtools/ksp/symbol/TestSource4GrandParent.java").toSourceFile(),
            )
        }.compile()
        assertEquals(result.messages, result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    private class TestGetFieldsProvider(
        private val withStaticField: Boolean,
        private val assert: (List<String>) -> Unit
    ) : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return Processor()
        }

        private inner class Processor() : SymbolProcessor {

            @OptIn(KspExperimental::class)
            override fun process(resolver: Resolver): List<KSAnnotated> {
                val name =
                    resolver.getKSNameFromString("com.lisb.google.devtools.ksp.symbol.TestSource4")
                val fields =
                    resolver.getClassDeclarationByName(name)!!
                        .getFields(withStaticField = withStaticField)
                        .toList()
                this@TestGetFieldsProvider.assert.invoke(fields.map { it.simpleName.asString() }
                    .sorted())
                return emptyList()
            }
        }
    }
}