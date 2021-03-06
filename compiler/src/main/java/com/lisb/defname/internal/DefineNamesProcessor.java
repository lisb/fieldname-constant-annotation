package com.lisb.defname.internal;

import com.lisb.defname.DefineName;
import com.lisb.defname.DefineNames;

import java.io.IOException;
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
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.lisb.defname.DefineNames")
public class DefineNamesProcessor extends AbstractProcessor {

	private Filer filer;
	private Elements elements;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.filer = processingEnv.getFiler();
		this.elements = processingEnv.getElementUtils();
	}

	@Override
	public boolean process(final Set<? extends TypeElement> elements,
			final RoundEnvironment env) {
		final Map<TypeElement, DefineNameClassWriter> classWriterMap = createClassWriter(env);
		build(classWriterMap);

		return true;
	}

	private void build(
			final Map<TypeElement, DefineNameClassWriter> classWriterMap) {
		for (final Entry<TypeElement, DefineNameClassWriter> entry : classWriterMap
				.entrySet()) {
			final TypeElement typeElement = entry.getKey();
			final DefineNameClassWriter classWriter = entry.getValue();
			writeToFile(typeElement, classWriter);
		}
	}

	private void writeToFile(final TypeElement typeElement,
			final DefineNameClassWriter classWriter) {
		JavaFileObject jfo = null;
		Writer writer = null;
		try {
			jfo = filer.createSourceFile(classWriter.getClassFQDN(),
					typeElement);
			writer = jfo.openWriter();
			classWriter.write(writer);
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

	private void logNote(String message) {
		if (processingEnv.getOptions().containsKey("debug")) {
			processingEnv.getMessager().printMessage(Kind.NOTE, message);
		}
	}

	private Map<TypeElement, DefineNameClassWriter> createClassWriter(
			final RoundEnvironment env) {
		final Map<TypeElement, DefineNameClassWriter> targetClassMap = new HashMap<TypeElement, DefineNameClassWriter>();
		for (final Element element : env
				.getElementsAnnotatedWith(DefineNames.class)) {
			final ElementKind kind = element.getKind();
			if (kind.isClass()) {
                final TypeElement typeElement = (TypeElement) element;
				logNote("Create defname of class \"" + typeElement.getSimpleName() + "\".");
                final boolean withStaticField = typeElement.getAnnotation(DefineNames.class).withStaticField();
                final DefineNameClassWriter classWriter = getOrCreateClassWriter(
                        targetClassMap, typeElement);
                final List<? extends Element> members = elements
                        .getAllMembers(typeElement);
                for (final Element member : members) {
                    if (!member.getKind().isField()
                            || (!withStaticField && member.getModifiers().contains(Modifier.STATIC))) {
                        continue;
                    }
                    final DefineName constant =
                            member.getAnnotation(DefineName.class);
                    if (constant != null) {
                        classWriter.addFields(constant.value());
                    } else {
                        classWriter
                                .addFields(member.getSimpleName().toString());
                    }
                }
            } else {
				processingEnv.getMessager().printMessage(Kind.ERROR,
						"kind is unsupported." + kind.name(), element);
			}
		}
		return targetClassMap;
	}

	private DefineNameClassWriter getOrCreateClassWriter(
			final Map<TypeElement, DefineNameClassWriter> classWriterMap,
			final TypeElement typeElement) {
		DefineNameClassWriter targetClass = classWriterMap.get(typeElement);
		if (targetClass == null) {
			final String packageName = ((PackageElement) typeElement
					.getEnclosingElement()).getQualifiedName().toString();
			final String classSimpleName = typeElement.getSimpleName()
					.toString();
			final DefineNames.Case[] cases = typeElement.getAnnotation(
					DefineNames.class).value();
			targetClass = new DefineNameClassWriter(packageName, classSimpleName, cases);
			classWriterMap.put(typeElement, targetClass);
		}
		return targetClass;
	}

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
