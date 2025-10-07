package com.lisb.google.devtools.ksp.symbol

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.lisb.defname.DefineNames
import com.lisb.google.devtools.ksp.symbol.KSClassDeclarationExt.getAllParentFiles
import com.lisb.google.devtools.ksp.symbol.KSClassDeclarationExt.getFields
import com.lisb.java.io.File.FileExt.toSourceFile
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.configureKsp
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
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
                File("src/test/java/com/lisb/defname/internal/TestSource3.kt").toSourceFile(),
                File("src/test/java/com/lisb/defname/internal/TestSource3Parent.kt").toSourceFile(),
                File("src/test/java/com/lisb/defname/internal/TestSource3GrandParent.kt").toSourceFile(),
                File("../annotation/src/main/java/com/lisb/defname/DefineNames.java").toSourceFile(),
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
                val name = DefineNames::class.qualifiedName
                if (name == null) {
                    fail("Cannot get qualified name of DefineNames")
                    return emptyList()
                }
                val ksAnnotated = resolver.getSymbolsWithAnnotation(annotationName = name).first()
                val parentFiles = (ksAnnotated as KSClassDeclaration).getAllParentFiles()
                assertEquals(
                    parentFiles.map { it.fileName }.sorted(),
                    listOf("TestSource3Parent.kt", "TestSource3GrandParent.kt").sorted()
                )
                return emptyList()
            }
        }
    }

    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun testGetFields() {
        val result = KotlinCompilation().apply {
            configureKsp(useKsp2 = true) {
                symbolProcessorProviders += TestGetFieldsProvider()
            }
            sources = listOf(
                File("src/test/java/com/lisb/defname/internal/TestSource4.java").toSourceFile(),
                File("src/test/java/com/lisb/defname/internal/TestSource4Parent.java").toSourceFile(),
                File("src/test/java/com/lisb/defname/internal/TestSource4GrandParent.java").toSourceFile(),
                File("../annotation/src/main/java/com/lisb/defname/DefineNames.java").toSourceFile(),
            )
        }.compile()
        assertEquals(result.messages, result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    private class TestGetFieldsProvider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return Processor()
        }

        private class Processor() : SymbolProcessor {

            @OptIn(KspExperimental::class)
            override fun process(resolver: Resolver): List<KSAnnotated> {
                val name = DefineNames::class.qualifiedName
                if (name == null) {
                    fail("Cannot get qualified name of DefineNames")
                    return emptyList()
                }
                val ksAnnotated = resolver.getSymbolsWithAnnotation(annotationName = name).first()
                val fields =
                    (ksAnnotated as KSClassDeclaration).getFields(withStaticField = true).toList()
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
                return emptyList()
            }
        }
    }
}