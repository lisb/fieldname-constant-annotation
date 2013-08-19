# Defname

apt を使いクラスのフィールドの名前一覧を定数として定義したクラスを自動生成します．

## 使用例

ベースとなるクラス:
```java
@DefineNames({ Case.Original, Case.SnakeCase })
public class Example {
    private int exampleId;
    @DefineName("displayName")
    private int name;
}
```

自動生成されるクラス:
```java
public class _CExample {
    public static String example_id = "example_id";
    public static String exampleId = "exampleId";
    public static String displayName = "displayName";
    public static String display_name = "display_name";
}
```