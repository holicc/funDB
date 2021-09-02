package org.holicc.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Command {

    String name();

    String subCommand() default "";

    String description() default "";

    boolean persistence() default false;

    int minimumArgs() default 1;
}