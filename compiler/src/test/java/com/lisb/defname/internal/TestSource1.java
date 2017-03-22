package com.lisb.defname.internal;

import com.lisb.defname.DefineNames;
import com.lisb.defname.DefineNames.Case;

@DefineNames({ Case.Original, Case.SnakeCase })
@SuppressWarnings("unused")
public class TestSource1 extends TestSource1Parent {
	private String test;
	private String testCase;
	private String TestCase;
    private static final String STATIC_FIELD = "static_field";
}
