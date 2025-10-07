package com.lisb.defname.internal;

import com.lisb.defname.DefineNames;

@SuppressWarnings("unused")
@DefineNames(withStaticField = true)
public class TestSource2 extends TestSource2Parent {
    private static final String STATIC_FIELD = "static_field";
}
