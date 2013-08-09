package com.lisb.constant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Constants {

	enum Case {
		SnakeCase, Original
	}

	Case[] value() default Case.Original;
}
