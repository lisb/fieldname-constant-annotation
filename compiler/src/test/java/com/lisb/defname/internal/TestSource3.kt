package com.lisb.defname.internal

import com.lisb.defname.DefineNames

@DefineNames(DefineNames.Case.SnakeCase)
@SuppressWarnings("unused")
class TestSource3 : TestSource3Parent() {
    val test: Long? = null
}