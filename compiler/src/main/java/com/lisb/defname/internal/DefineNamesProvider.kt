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
import java.io.IOException
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
                for (property in ksAnnotated.getAllProperties()) {
                    val annotation = property.getAnnotationsByType(DefineName::class).firstOrNull()
                    if (annotation == null) {
                        classWriter.addFields(property.simpleName.asString())
                    } else {
                        classWriter.addFields(annotation.value)
                    }
                }
                val dependencies = Dependencies(false, requireNotNull(ksAnnotated.containingFile))
                try {
                    environment.codeGenerator.createNewFile(
                        dependencies,
                        ksAnnotated.packageName.asString(),
                        classWriter.className,
                        extensionName = "java"
                    ).use { writer ->
                        classWriter.write(OutputStreamWriter(writer))
                    }
                } catch (_: IOException) {
                    // Handle the exception, possibly logging it
                }
            }
            return emptyList()
        }
    }
}