package com.wiredi.compiler.domain;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeSpec;
import com.wiredi.compiler.domain.entities.IdentifiableProviderEntity;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;
import jakarta.annotation.Generated;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public abstract class AbstractClassEntity<T extends ClassEntity> implements ClassEntity {

	protected final TypeSpec.Builder builder;
	private final TypeMirror rootElement;
	public final String className;
	private PackageElement packageElement;

	public AbstractClassEntity(TypeMirror rootElement, String className) {
		this.className = className;
		this.builder = createBuilder(rootElement)
				.addAnnotation(generatedAnnotation());

		List<Class<?>> autoServiceType = autoServiceTypes();
		if (!autoServiceType.isEmpty()) {
			builder.addAnnotation(autoServiceAnnotation(autoServiceType));
		}

		this.rootElement = rootElement;
	}

	@Override
	public Optional<PackageElement> packageElement() {
		return Optional.ofNullable(this.packageElement);
	}

	public T appendMethod(MethodFactory methodFactory) {
		methodFactory.append(builder, this);
		return (T) this;
	}

	public T setPackageOf(Element element) {
		return setPackage(TypeUtils.packageOf(element));
	}

	public T setPackage(PackageElement packageElement) {
		addSource(packageElement);
		this.packageElement = packageElement;
		return (T) this;
	}

	public T addSource(Element element) {
		builder.addOriginatingElement(element);
		return (T) this;
	}

	@Override
	public ClassName className() {
		return packageElement()
				.map(p -> ClassName.get(p.getQualifiedName().toString(), className))
				.orElseThrow(() -> new IllegalStateException("Package not set"));
	}

	protected abstract TypeSpec.Builder createBuilder(TypeMirror type);

	protected void finalize(TypeSpec.Builder builder) {
	}

	private AnnotationSpec generatedAnnotation() {
		AnnotationSpec.Builder generatedBuilder = AnnotationSpec.builder(Generated.class)
				.addMember("value", "$S", getClass().getName())
				.addMember("date", "$S", ZonedDateTime.now().toString());

		String comments = comments();
		if (comments != null) {
			generatedBuilder.addMember("comments", "$S", comments);
		}

		return generatedBuilder.build();
	}

	private AnnotationSpec autoServiceAnnotation(List<Class<?>> types) {
		return AnnotationSpec.builder(AutoService.class)
				.addMember("value", "{$L}", CodeBlock.join(types.stream().map(type -> CodeBlock.builder().add("$T.class", type).build()).toList(), ", "))
				.build();
	}

	public boolean willHaveTheSamePackageAs(Element element) {
		return packageElement()
				.map(p -> p.equals(packageElementOf(element)))
				.orElse(false);
	}

	public PackageElement packageElementOf(Element element) {
		Element current = element;
		while (!(current instanceof PackageElement)) {
			current = current.getEnclosingElement();
		}

		return (PackageElement) current;
	}

	@Override
	public final TypeSpec build() {
		finalize(builder);
		return builder.build();
	}

	@Override
	public final TypeMirror rootType() {
		return rootElement;
	}

	@Nullable
	public String comments() {
		return null;
	}

	@NotNull
	public List<Class<?>> autoServiceTypes() {
		return List.of();
	}

	public boolean requiresReflectionFor(ExecutableElement element) {
		if (element.getModifiers().contains(Modifier.PUBLIC)) {
			return false;
		}

		return packageElement().map(it -> it.equals(packageElementOf(element))).orElse(true);
	}

	public boolean requiresReflectionFor(Element element) {
		if (element.getModifiers().contains(Modifier.PUBLIC)) {
			return false;
		}

		return packageElement().map(it -> it.equals(packageElementOf(element))).orElse(true);
	}
}
