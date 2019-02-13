package micronaut.problems

import com.stehno.ersatz.ErsatzServer
import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Specification

class ClientSpec extends Specification {
    @AutoCleanup ErsatzServer server
    @AutoCleanup ApplicationContext context

    def setup() {
        server = new ErsatzServer()
        server.expectations {
            get("/") {
                called(1)

                responder {
                    body("hi")
                }
            }
        }
        server.start()

        context = ApplicationContext
            .build()
            .properties("foo.bar": server.getHttpUrl())
            .build()
            .start()
    }

    def "Declarative client"() {
        given:
        def client = context.getBean(DeclarativeClient) // works fine

        when:
        def response = client.hello()

        then:
        response == "hi"
    }

    // issue https://github.com/micronaut-projects/micronaut-core/issues/1144
    def "Low level client"() {
        given:
        def client = context.getBean(LowLevelClient) // works from >= 1.0.4

        when:
        def response = client.hello()

        then:
        response == "hi"
    }
}