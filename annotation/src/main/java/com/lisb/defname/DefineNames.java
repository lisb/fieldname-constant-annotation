package com.lisb.defname;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface DefineNames {

	enum Case {
		SnakeCase, Original
	}

	Case[] value() default Case.Original;
    boolean withStaticField() default false;
}
