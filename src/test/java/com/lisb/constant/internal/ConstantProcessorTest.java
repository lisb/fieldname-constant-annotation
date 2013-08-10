package com.lisb.constant.internal;

import java.nio.charset.Charset;
import java.util.Locale;

import org.seasar.aptina.unit.AptinaTestCase;

public class ConstantProcessorTest extends AptinaTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// 言語設定
		setLocale(Locale.JAPANESE);
		setCharset(Charset.forName("UTF-8"));

		// ソースパスを追加
		addSourcePath("src/test/java");
	}

	public void testProcess() throws Exception {
		// テスト対象の Annotation Processor を生成して追加
		final ConstantProcessor processor = new ConstantProcessor(true);
		addProcessor(processor);

		// コンパイル対象を追加
		addCompilationUnit(TestSource.class);

		// コンパイル実行
		compile();

		// テスト対象の Annotation Processor が生成したソースを検証
		assertEqualsGeneratedSourceWithResource(
				ConstantProcessorTest.class.getResource("TestSource$$C.java"),
				"com.lisb.constant.internal.TestSource$$C");
	}

}
