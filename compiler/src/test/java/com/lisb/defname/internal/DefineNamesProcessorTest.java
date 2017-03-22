package com.lisb.defname.internal;

import org.seasar.aptina.unit.AptinaTestCase;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

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
        final DefineNamesProcessor processor = new DefineNamesProcessor();
        addProcessor(processor);

        // コンパイル対象を追加
        addCompilationUnit(TestSource1Parent.class);
        addCompilationUnit(TestSource1.class);
        addCompilationUnit(TestSource2.class);

        // コンパイル実行
        compile();

        // テスト対象の Annotation Processor が生成したソースを検証
        assertEqualsGeneratedSourceWithResource(getClass().getResource("Expected_CTestSource1.java"),
                "com.lisb.defname.internal._CTestSource1");
        assertEqualsGeneratedSourceWithResource(getClass().getResource("Expected_CTestSource2.java"),
                "com.lisb.defname.internal._CTestSource2");
    }

    private void testProcess(Class<?> target, URL expectedResourceUrl, String exportFilename) throws Exception {
        // テスト対象の Annotation Processor を生成して追加
        final DefineNamesProcessor processor = new DefineNamesProcessor();
        addProcessor(processor);

        // コンパイル対象を追加
        addCompilationUnit(target);

        // コンパイル実行
        compile();

        // テスト対象の Annotation Processor が生成したソースを検証
        assertEqualsGeneratedSourceWithResource(expectedResourceUrl, exportFilename);
    }
}
