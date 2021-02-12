##Laying out your components with Accordion
@Accordion annotation

To define array of `@AccordionPanel` within `@Dialog` or `@AccordionWidget` annotation. Then, to settle a field to a certain accordionPanel you will need  to add `@PlaceOn` annotation to this particular field.  The values of `@PlaceOn` must correspond to the *title* value of the desired accordionPanel. This is a somewhat more flexible technique which avoids creating nested classes and allows freely moving fields. You only need to ensure that panel title is specified everywhere in the very same format, no extra spaces, etc.
```java
@Dialog(
    name = "test-component",
    title = "test-component-dialog",
    panels = {
        @AccordionPanel(title = "First accordionPanel")
    }
)
public class TestAccordion {
    @DialogField(label = "Field on the first tab")
    @TextField
    @PlaceOn("First accordionPanel")
    String field1;

    @AccordionWidget(
        panels = {
            @AccordionPanel(title = "Accordion Widget Panel")
        }
    )
    AccordionExample accordionExample;

    static class AccordionExample {
          @PlaceOn("Accordion Widget Panel")
          @DialogField
          @TextField
          String field6;
    }
}
```


