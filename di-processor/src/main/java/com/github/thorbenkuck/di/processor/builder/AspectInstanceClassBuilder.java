package com.github.thorbenkuck.di.processor.builder;

import com.github.thorbenkuck.di.aspects.AspectInstance;
import com.github.thorbenkuck.di.aspects.ExecutionContext;
import com.github.thorbenkuck.di.processor.foundation.ProcessorContext;
import com.squareup.javapoet.*;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class AspectInstanceClassBuilder {

    private final TypeMirror annotationType;
    private final TypeElement containingClass;
    private final ExecutableElement methodToExecute;
    private final TypeSpec.Builder typeSpecBuilder;
    private final String className;

    private static final String delegateName = "delegate";

    public AspectInstanceClassBuilder(TypeMirror annotationType, TypeElement containingClass, ExecutableElement methodToExecute) {
        this.annotationType = annotationType;
        this.containingClass = containingClass;
        this.methodToExecute = methodToExecute;
        TypeElement typeElement = ProcessorContext.getElements().getTypeElement(annotationType.toString());
        this.className = typeElement.getSimpleName() +
                "On" + methodToExecute.getSimpleName() +
                "In" + containingClass.getSimpleName() +
                "Aspect";

        typeSpecBuilder = TypeSpec.classBuilder(this.className)
                .addModifiers(Modifier.FINAL, Modifier.PRIVATE, Modifier.STATIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(AspectInstance.class), ClassName.get(annotationType)));
    }

    private ParameterizedTypeName aspectContextType() {
        return ParameterizedTypeName.get(ClassName.get(ExecutionContext.class), ClassName.get(annotationType));
    }

    public AspectInstanceClassBuilder addDelegateField() {
        typeSpecBuilder
                .addField(
                        FieldSpec.builder(ClassName.get(containingClass), delegateName)
                                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                                .build()
                );

        return this;
    }

    public AspectInstanceClassBuilder addConstructor() {
        typeSpecBuilder
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addParameter(ClassName.get(containingClass), delegateName, Modifier.FINAL)
                                .addModifiers(Modifier.PRIVATE)
                                .addCode(CodeBlock.builder()
                                        .addStatement("this.$L = $L", delegateName, delegateName)
                                        .build())
                                .build()
                );

        return this;
    }

    public AspectInstanceClassBuilder addProcessMethod() {
        typeSpecBuilder.addMethod(
                        MethodSpec.methodBuilder("process")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                .addParameter(
                                        ParameterSpec.builder(aspectContextType(), "context")
                                                .addModifiers(Modifier.FINAL)
                                                .build())
                                .returns(ClassName.OBJECT)
                                .addCode(CodeBlock.builder()
                                        .addStatement("return $L.$L(context)", delegateName, methodToExecute.getSimpleName())
                                        .build())
                                .build()
                );

        return this;
    }

    public TypeSpec build() {
        return typeSpecBuilder.build();
    }
}
