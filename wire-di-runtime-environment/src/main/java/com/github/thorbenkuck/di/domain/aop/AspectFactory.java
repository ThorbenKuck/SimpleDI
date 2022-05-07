package com.github.thorbenkuck.di.domain.aop;

import com.github.thorbenkuck.di.runtime.WireRepository;
import com.github.thorbenkuck.di.aspects.AspectInstance;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

public interface AspectFactory<T extends Annotation> {

    @NotNull
    AspectInstance<T> build(@NotNull final WireRepository wireRepository);

    @NotNull
    Class<T> aroundAnnotation();

}