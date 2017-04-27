package com.holgerhees.persistance.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DbColumn {

	String name();
	String type();
	boolean nullable() default false;
	boolean insertable() default true;
	boolean updatable() default true;
	DbForeignKey[] foreignKey() default {};
}
