package micronaut.problems;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

@Client("${property.a}")
public interface HttpClientUsingPropertyA {
    @Get
    String callSomething();
}

