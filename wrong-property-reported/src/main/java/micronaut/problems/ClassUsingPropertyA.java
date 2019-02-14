package micronaut.problems;

import io.micronaut.context.annotation.Property;

import javax.inject.Singleton;

@Singleton // singleton is needed to make it a proper bean
public class ClassUsingPropertyA {

    @Property(name = "property.a")
    private String property;

    public String getProperty() {
        return property;
    }
}

