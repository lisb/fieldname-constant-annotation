package com.lisb.defname;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
public @interface DefineName {

	String value();

}
