package com.lisb.defname.internal

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.lisb.defname.DefineName
import com.lisb.defname.DefineNames
import com.lisb.google.devtools.ksp.symbol.KSClassDeclarationExt.getAllParentFiles
import com.lisb.google.devtools.ksp.symbol.KSClassDeclarationExt.getFields
import java.io.OutputStreamWriter

class DefineNamesProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DefineNamesProcessor(environment)
    }

    private class DefineNamesProcessor(
        private val environment: SymbolProcessorEnvironment
    ) : SymbolProcessor {

        @OptIn(KspExperimental::class)
        override fun process(resolver: Resolver): List<KSAnnotated> {
            val name = DefineNames::class.qualifiedName ?: return emptyList()
            val ksAnnotatedSequence = resolver.getSymbolsWithAnnotation(annotationName = name)
            for (ksAnnotated in ksAnnotatedSequence) {
                if (ksAnnotated !is KSClassDeclaration) continue
                val defineNamesAnnotation = ksAnnotated.getAnnotationsByType(annotationKClass = DefineNames::class).firstOrNull() ?: continue
                val classWriter = DefineNameClassWriter(
                    ksAnnotated.packageName.asString(),
                    ksAnnotated.simpleName.asString(),
                    defineNamesAnnotation.value
                )
                for (property in ksAnnotated.getFields(defineNamesAnnotation.withStaticField)) {
                    val annotation = property.getAnnotationsByType(DefineName::class).firstOrNull()
                    val fieldName = annotation?.value ?: property.simpleName.asString()
                    classWriter.addFields(fieldName)
                }
                val parentFiles = ksAnnotated.getAllParentFiles()
                val allFiles =
                    ksAnnotated.containingFile?.let { parentFiles.plus(it) } ?: parentFiles
                val dependencies =
                    Dependencies(aggregating = false, sources = allFiles.toTypedArray())
                environment.codeGenerator.createNewFile(
                    dependencies,
                    ksAnnotated.packageName.asString(),
                    classWriter.className,
                    extensionName = FILE_EXTENSION_NAME,
                ).use { writer ->
                    classWriter.write(OutputStreamWriter(writer))
                }
            }
            return emptyList()
        }
    }

    companion object {
        private const val FILE_EXTENSION_NAME = "java"
    }
}