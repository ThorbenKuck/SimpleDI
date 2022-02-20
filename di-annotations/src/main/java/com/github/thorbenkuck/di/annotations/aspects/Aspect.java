package com.github.thorbenkuck.di.annotations.aspects;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Aspect {

    Class<? extends Annotation> around();

}
