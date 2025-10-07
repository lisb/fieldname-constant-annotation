package com.lisb.defname.internal;

import com.lisb.defname.DefineNames;

@SuppressWarnings("unused")
@DefineNames(withStaticField = true)
public class TestSource4 extends TestSource4Parent {
    private String PrivateTestSource;
    String PublicTestSource;
    private static final String STATIC_FIELD = "static_field";
}
