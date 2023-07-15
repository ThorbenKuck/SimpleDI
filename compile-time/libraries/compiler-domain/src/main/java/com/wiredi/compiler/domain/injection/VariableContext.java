package com.wiredi.compiler.domain.injection;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import com.wiredi.annotations.properties.Resolve;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.Qualifiers;
import com.wiredi.compiler.domain.TypeUtils;
import com.wiredi.compiler.domain.WireRepositories;
import com.wiredi.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VariableContext {

	@NotNull
	private final NameContext nameContext = new NameContext();

	@NotNull
	private final String varPrefix;

	@NotNull
	private final Map<TypeMirror, Map<QualifierType, String>> qualifiedCache = new HashMap<>();

	@NotNull
	private final Map<TypeMirror, String> unqualifiedCache = new HashMap<>();

	@NotNull
	private final Map<String, String> resolveCache = new HashMap<>();

	public VariableContext(@NotNull String varPrefix) {
		this.varPrefix = varPrefix;
	}

	public VariableContext() {
		this("variable");
	}

	public String instantiateVariableIfRequired(Element element, WireRepositories wireRepositories, CodeBlock.Builder codeBlock) {
		return instantiateVariableIfRequired(element, (name, qualifier) -> {
			codeBlock.addStatement("$T $L = $L", element.asType(), name, wireRepositories.fetchFromWireRepository(element, qualifier));
		}, (name, resolve) -> {
			codeBlock.addStatement("$T $L = $L", element.asType(), name, wireRepositories.resolveFromEnvironment(element, resolve));
		});
	}

	public String instantiateVariableIfRequired(
			Element element,
			BiConsumer<String, QualifierType> newNameFunction,
			BiConsumer<String, String> newPropertyFunction
	) {
		Optional<Resolve> annotation = Annotations.getAnnotation(element, Resolve.class);
		if (annotation.isPresent()) {
			Resolve resolve = annotation.get();
			return resolveCache.computeIfAbsent(resolve.value(), (value) -> {
				String nextName = nameContext.nextName(varPrefix);
				newPropertyFunction.accept(nextName, value);
				return nextName;
			});
		} else {
			return instantiateVariableIfRequired(Qualifiers.injectionQualifier(element), element.asType(), newNameFunction);
		}
	}

	/**
	 * Allows for single instantiations of variables and therefore reusing existing variables.
	 * <p>
	 * The neNameFunction will be invoked, if a new variable name is created.
	 *
	 * @param typeMirror      the type of the variable
	 * @param newNameFunction the function to invoke if a new variable is created
	 * @return the name associated with the typeMirror
	 */
	public String instantiateVariableIfRequired(
			QualifierType qualifierType,
			TypeMirror typeMirror,
			BiConsumer<String, QualifierType> newNameFunction
	) {
		if (TypeUtils.isSingleton(typeMirror)) {
			Map<QualifierType, String> qualifierMap = qualifiedCache.computeIfAbsent(typeMirror, (t) -> new HashMap<>());
			return qualifierMap.computeIfAbsent(qualifierType, (typeName) -> {
				String nextName = nameContext.nextName(varPrefix);
				newNameFunction.accept(nextName, qualifierType);
				return nextName;
			});
		} else {
			String nextName = nameContext.nextName(varPrefix);
			newNameFunction.accept(nextName, qualifierType);
			return nextName;
		}
	}
}