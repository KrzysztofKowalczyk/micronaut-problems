package micronaut.problems;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;

@Client("${foo.bar}")
public interface DeclarativeClient {

  @Get
  String hello();
}
