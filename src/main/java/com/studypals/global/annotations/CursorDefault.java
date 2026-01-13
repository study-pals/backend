package com.studypals.global.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.studypals.global.request.SortType;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CursorDefault {
    Class<? extends SortType> sortType();

    long cursor() default 0;

    int size() default 10;

    String sort() default "NEW";

    String value() default "";
}
