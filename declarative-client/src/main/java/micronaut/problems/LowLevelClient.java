package micronaut.problems;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;

import javax.inject.Inject;

public class LowLevelClient {

    @Inject @Client("${foo.bar}")
    RxHttpClient client;

    public String hello() {
        HttpRequest<?> req = HttpRequest.GET("/");
        return client.exchange(req, String.class).blockingFirst().body();
    }
}
