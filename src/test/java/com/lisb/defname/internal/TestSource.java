package com.lisb.defname.internal;

import com.lisb.defname.DefineNames;
import com.lisb.defname.DefineNames.Case;

@DefineNames({ Case.Original, Case.SnakeCase })
@SuppressWarnings("unused")
public class TestSource extends TestSourceParent {
	private String test;
	private String testCase;
	private String TestCase;
}
