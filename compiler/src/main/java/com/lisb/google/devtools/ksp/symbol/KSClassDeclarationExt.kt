package com.lisb.google.devtools.ksp.symbol

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

object KSClassDeclarationExt {
    fun KSClassDeclaration.getFields(withStaticField: Boolean): Sequence<KSPropertyDeclaration> {
        return if (withStaticField) {
            this.declarations.filterIsInstance<KSPropertyDeclaration>()
        } else {
            this.getAllProperties()
        }
    }
}
