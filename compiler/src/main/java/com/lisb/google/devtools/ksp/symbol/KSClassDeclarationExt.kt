package com.lisb.google.devtools.ksp.symbol

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier

object KSClassDeclarationExt {
    fun KSClassDeclaration.getFields(withStaticField: Boolean): List<KSPropertyDeclaration> {
        return if (withStaticField) {
            this.getAllFieldsIncludingStatic()
        } else {
            this.getAllProperties().toList()
        }
    }

    fun KSClassDeclaration.getAllFieldsIncludingStatic(): List<KSPropertyDeclaration> {
        return mutableListOf<KSPropertyDeclaration>().apply {
            getAllFieldsIncludingStatic(this, false)
        }
    }

    /**
     * @param isInherited 継承されたクラスか否か(継承されたクラスの場合、Private フィールドは無視する)
     */
    private fun KSClassDeclaration.getAllFieldsIncludingStatic(
        fields: MutableList<KSPropertyDeclaration>,
        isInherited: Boolean
    ) {
        val declarations = this.declarations.filterIsInstance<KSPropertyDeclaration>()
        for (declaration in declarations) {
            if (fields.any { it.simpleName.asString() == declaration.simpleName.asString() }) continue
            if (isInherited && declaration.modifiers.contains(Modifier.PRIVATE)) continue
            fields.add(declaration)
        }
        val parent =
            this.superTypes.firstNotNullOfOrNull { it.resolve().declaration as? KSClassDeclaration }
        parent?.getAllFieldsIncludingStatic(fields, true)
    }

    fun KSClassDeclaration.getAllParentFiles(): List<KSFile> {
        val parents = mutableListOf<KSFile>()
        for (parent in this.superTypes.mapNotNull { it.resolve().declaration as? KSClassDeclaration }) {
            parent.containingFile?.let { parents.add(it) }
            parents.addAll(parent.getAllParentFiles())
        }
        return parents
    }
}
