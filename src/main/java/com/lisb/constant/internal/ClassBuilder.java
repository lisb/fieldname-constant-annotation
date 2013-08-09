package com.lisb.constant.internal;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lisb.constant.Constants;
import com.squareup.java.JavaWriter;

class ClassBuilder {

	private static final String GENERATE_CLASS_SUFFIX = "$$C";

	private final String packageName;
	private final String targetClass;
	private final Constants.Case[] cases;
	private final boolean isTest;
	private final Set<String> fields = new HashSet<String>();

	ClassBuilder(final String packageName, final String targetClass,
			final Constants.Case[] cases) {
		this(packageName, targetClass, cases, false);
	}

	ClassBuilder(final String packageName, final String targetClass,
			final Constants.Case[] cases, final boolean isTest) {
		this.packageName = packageName;
		this.targetClass = targetClass;
		this.cases = cases;
		this.isTest = isTest;
	}

	void addFields(final String field) {
		for (final Constants.Case c : cases) {
			final String fieldName = c == Constants.Case.Original ? field
					: convertToSnakeCase(field);
			fields.add(fieldName);
		}
	}

	String getClassFQDN() {
		return targetClass + GENERATE_CLASS_SUFFIX;
	}

	void build(final Writer writer) throws IOException {
		JavaWriter javaWriter = null;
		try {
			javaWriter = new JavaWriter(writer);
			javaWriter.emitPackage(packageName).beginType(getClassFQDN(),
					"class", Modifier.PUBLIC);
			final Collection<String> fields;
			if (isTest) {
				// テスト時はフィールドの並び順が予測可能なようにソートする
				final List<String> list = new ArrayList<String>(this.fields);
				Collections.sort(list);
				fields = list;
			} else {
				fields = this.fields;
			}

			for (final String field : fields) {
				javaWriter
						.emitField("String", field, Modifier.PUBLIC
								| Modifier.STATIC | Modifier.FINAL, "\""
								+ field + "\"");
			}
			javaWriter.endType();
		} finally {
			if (javaWriter != null) {
				try {
					javaWriter.close();
				} catch (IOException e) {

				}
			}
		}
	}

	private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");

	static final String convertToSnakeCase(final String original) {
		final StringBuffer sb = new StringBuffer();
		final Matcher m = UPPERCASE.matcher(original);
		while (m.find()) {
			if (m.start() != 0) {
				m.appendReplacement(sb, "_");
			} else {
				m.appendReplacement(sb, "");
			}
			sb.append(m.group().toLowerCase());
		}
		m.appendTail(sb);
		return sb.toString();
	}
}