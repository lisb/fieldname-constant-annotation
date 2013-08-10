package com.lisb.constant.internal;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.lisb.constant.Constant;
import com.lisb.constant.Constants;

@SupportedAnnotationTypes("com.lisb.constant.Constants")
public class ConstantProcessor extends AbstractProcessor {

	private Filer filer;
	private Elements elements;
	private final boolean isTest;

	public ConstantProcessor() {
		this(false);
	}

	public ConstantProcessor(boolean isTest) {
		this.isTest = isTest;
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.filer = processingEnv.getFiler();
		this.elements = processingEnv.getElementUtils();
	}

	@Override
	public boolean process(final Set<? extends TypeElement> elements,
			final RoundEnvironment env) {
		final Map<TypeElement, ClassBuilder> classBuilderMap = createClassBuilder(env);
		build(classBuilderMap);

		return true;
	}

	private void build(final Map<TypeElement, ClassBuilder> classBuilderMap) {
		for (final Entry<TypeElement, ClassBuilder> entry : classBuilderMap
				.entrySet()) {
			final TypeElement typeElement = entry.getKey();
			final ClassBuilder classBuilder = entry.getValue();
			writeToFile(typeElement, classBuilder);
			if (isTest) {
				writeToStdout(typeElement, classBuilder);
			}
		}
	}

	private void writeToFile(final TypeElement typeElement,
			final ClassBuilder classBuilder) {
		JavaFileObject jfo = null;
		Writer writer = null;
		try {
			jfo = filer.createSourceFile(classBuilder.getClassFQDN(),
					typeElement);
			writer = jfo.openWriter();
			classBuilder.build(writer);
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Kind.ERROR,
					"IOException is occured." + e.getMessage(), typeElement);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {

				}
			}
		}
	}

	private void writeToStdout(final TypeElement typeElement,
			final ClassBuilder classBuilder) {
		final Writer writer = new OutputStreamWriter(System.out);
		try {
			classBuilder.build(writer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {

			}
		}
	}

	private Map<TypeElement, ClassBuilder> createClassBuilder(
			final RoundEnvironment env) {
		final Map<TypeElement, ClassBuilder> targetClassMap = new HashMap<TypeElement, ClassBuilder>();
		for (final Element element : env
				.getElementsAnnotatedWith(Constants.class)) {
			final ElementKind kind = element.getKind();
			if (kind.isClass()) {
				final TypeElement typeElement = (TypeElement) element;
				processingEnv.getMessager().printMessage(Kind.NOTE,
						"kind is class.", element);
				final ClassBuilder classBuilder = getOrCreateClassBuilder(
						targetClassMap, typeElement);
				final List<? extends Element> members = elements
						.getAllMembers(typeElement);
				for (final Element member : members) {
					if (!member.getKind().isField()) {
						continue;
					}
					final Constant constant = member
							.getAnnotation(Constant.class);
					if (constant != null) {
						classBuilder.addFields(constant.value());
					} else {
						classBuilder.addFields(member.getSimpleName()
								.toString());
					}
				}
			} else {
				processingEnv.getMessager().printMessage(Kind.ERROR,
						"kind is unsupported." + kind.name(), element);
			}
		}
		return targetClassMap;
	}

	private ClassBuilder getOrCreateClassBuilder(
			final Map<TypeElement, ClassBuilder> classBuilderMap,
			final TypeElement typeElement) {
		ClassBuilder targetClass = classBuilderMap.get(typeElement);
		if (targetClass == null) {
			final String packageName = ((PackageElement) typeElement
					.getEnclosingElement()).getQualifiedName().toString();
			final String className = typeElement.getQualifiedName().toString();
			final Constants.Case[] cases = typeElement.getAnnotation(
					Constants.class).value();
			targetClass = new ClassBuilder(packageName, className, cases,
					isTest);
			classBuilderMap.put(typeElement, targetClass);
		}
		return targetClass;
	}
}
