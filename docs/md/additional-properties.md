[Main page](../../README.md)
## Common attributes of fields
Components TouchUI dialogs honor the concept of [global HTML attributes](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/docs/server/commonattrs.html) added to rendered HTML tags. To set them via AEM-Dialog-Plugin, you use the @Attribute annotation.
```java
public class DialogWithHtmlAttributes {
    @DialogField
    @TextField
    @Attribute(
        id = "field1-id",
        className = "field1-attribute-class",
        data = {
            @Data(name = "field1-data1", value = "value-data1"),
            @Data(name = "field1-data2", value = "value-data2")
    })
    String field1;
}
```
