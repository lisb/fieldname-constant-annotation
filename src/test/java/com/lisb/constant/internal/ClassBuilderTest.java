package com.lisb.constant.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

import com.lisb.constant.Constants.Case;

public class ClassBuilderTest {

	@Test
	public void testConvertToSnakeCase() {
		Assert.assertEquals("test", ClassBuilder.convertToSnakeCase("test"));
		Assert.assertEquals("test_case",
				ClassBuilder.convertToSnakeCase("testCase"));
		Assert.assertEquals("test_case",
				ClassBuilder.convertToSnakeCase("TestCase"));
		Assert.assertEquals("test_case_for_class_builder",
				ClassBuilder.convertToSnakeCase("testCaseForClassBuilder"));
	}

	@Test
	public void testBuild() throws IOException {
		final ClassBuilder builder = new ClassBuilder(
				"com.lisb.constant.internal", "ClassBuilderTestSource",
				new Case[] { Case.Original, Case.SnakeCase });
		builder.addFields("test");
		builder.addFields("testCase");
		builder.addFields("TestCase");
		builder.addFields("testCaseForClassBuilder");
		final StringWriter writer = new StringWriter();
		builder.build(writer);
		writer.flush();
		writer.close();

		Assert.assertEquals(getTestSource("ClassBuilderTestSource$$C.java"),
				writer.toString());
	}

	private String getTestSource(final String className) throws IOException {
		final InputStream in = getClass().getResourceAsStream(className);
		final Reader reader = new InputStreamReader(in);
		final BufferedReader bReader = new BufferedReader(reader);
		try {
			int size;
			final char[] buffer = new char[1024];
			final StringBuilder sb = new StringBuilder();
			while ((size = bReader.read(buffer)) != -1) {
				sb.append(buffer, 0, size);
			}
			return sb.toString();
		} finally {
			bReader.close();
			reader.close();
			in.close();
		}
	}

}
