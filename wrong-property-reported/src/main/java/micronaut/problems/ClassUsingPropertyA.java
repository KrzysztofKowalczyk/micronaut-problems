package micronaut.problems;

import io.micronaut.context.annotation.Property;

public class ClassUsingPropertyA {

    @Property(name = "property.a")
    private String property;

    public String getProperty() {
        return property;
    }
}

