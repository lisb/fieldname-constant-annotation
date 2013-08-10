package com.lisb.defname.internal;

import java.nio.charset.Charset;
import java.util.Locale;

import org.seasar.aptina.unit.AptinaTestCase;

import com.lisb.defname.internal.DefineNamesProcessor;

public class DefineNamesProcessorTest extends AptinaTestCase {

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
		final DefineNamesProcessor processor = new DefineNamesProcessor(true);
		addProcessor(processor);

		// コンパイル対象を追加
		addCompilationUnit(TestSource.class);

		// コンパイル実行
		compile();

		// テスト対象の Annotation Processor が生成したソースを検証
		assertEqualsGeneratedSourceWithResource(
				DefineNamesProcessorTest.class.getResource("TestSource$$C.java"),
				"com.lisb.defname.internal.TestSource$$C");
	}

}
