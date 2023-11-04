package com.wiredi.aspects;

import com.wiredi.aspects.links.RootMethod;
import com.wiredi.domain.AnnotationMetaData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class ExecutionChainTest {

	private final RootMethod rootMethod = RootMethod.newInstance("test").build(context -> context.requireParameter("param"));
	private final ExampleClass exampleClass = new ExampleClass();
	private final ExampleAnnotation annotation = exampleClass.getClass().getAnnotation(ExampleAnnotation.class);
	private final AnnotationMetaData annotationMetaData = AnnotationMetaData.empty(ExampleAnnotation.class.getName());


	@Test
	public void test() {
		// Arrange
		// Act
		String result = ExecutionChain.newInstance(rootMethod)
				.withProcessor(annotation, context -> context.proceed() + " From")
				.withProcessor(annotationMetaData, context -> context.proceed() + " Aspects")
				.build()
				.execute(Map.of("param", "Hello World"), String.class);

		// Assert
		assertThat(result).isEqualTo("Hello World From Aspects");
	}

	@Test
	@DisplayName("Verify that the distinct builder ignores duplicates")
	public void testDistinctBuilder() {
		// Arrange
		AspectHandler handler = context -> "ADD+" + context.proceed();
		String input = "Hello World";

		// Act
		String result = ExecutionChain.newInstance(rootMethod)
				.withProcessor(annotation, handler)
				.withProcessor(annotation, handler)
				.withProcessor(annotation, handler)
				.withProcessor(annotation, handler)
				.distinct(true)
				.build()
				.execute(Map.of("param", input), String.class);

		// Assert
		assertThat(result).isEqualTo("ADD+" + input);
	}

	@Test
	@DisplayName("Verify that an indistinct builder respects duplicates")
	public void testIndistinctBuilder() {
		// Arrange
		AspectHandler handler = context -> "ADD+" + context.proceed();
		String input = "Hello World";

		// Act
		String result = ExecutionChain.newInstance(rootMethod)
				.withProcessor(annotation, handler)
				.withProcessor(annotation, handler)
				.withProcessor(annotation, handler)
				.withProcessor(annotation, handler)
				.distinct(false)
				.build()
				.execute(Map.of("param", input), String.class);

		// Assert
		assertThat(result).isEqualTo("ADD+ADD+ADD+ADD+" + input);
	}

	@TestFactory
	public Stream<DynamicTest> testThatAnExecutionChainCanBeReused() {
		int rounds = 10;

		ExecutionChain executionChain = ExecutionChain.newInstance(rootMethod)
				.withProcessor(annotation, context -> context.proceed() + " From")
				.withProcessor(annotation, context -> context.proceed() + " Aspects")
				.build();

		return IntStream.range(0, rounds).mapToObj(i -> {
			String expected = i + " From Aspects";
			return dynamicTest("Run " + i + " => " + expected,  () -> {
						// Act
						String result = executionChain.execute(Map.of("param", i));

						// Assert
						assertThat(result).isEqualTo(expected);
					});
				}
		);
	}

	@Test
	public void testPrependingList() {
		// Arrange
		// Act
		String result = ExecutionChain.newInstance(rootMethod)
				.withProcessor(annotation, context -> context.proceed() + " From")
				.withProcessor(annotation, context -> context.proceed() + " Aspects")
				.build()
				.execute()
				.withParameter("param", "Hello World")
				.andReturn();

		// Assert
		assertThat(result).isEqualTo("Hello World From Aspects");
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface ExampleAnnotation {
	}

	@ExampleAnnotation
	static class ExampleClass {
	}
}