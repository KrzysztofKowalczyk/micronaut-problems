package micronaut.problems;

import io.micronaut.context.annotation.Value;

public class ClassUsingValueOfPropertyA {

    @Value("${property.a}")
    private String property;

    public String getProperty() {
        return property;
    }
}

