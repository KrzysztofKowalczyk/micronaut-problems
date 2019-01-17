package micronaut.problems

import com.stehno.ersatz.ErsatzServer
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.PropertySource
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
            .propertySources(PropertySource.of("foo.bar": server.getHttpUrl()))
            .build()
            .start()
    }

    def "Declarative client"() {
        given:
        def client = context.getBean(DeclarativeClient)

        when:
        def response = client.hello()

        then:
        response == "hi"
    }

    def "Low level client"() {
        given:
        def client = context.getBean(LowLevelClient)

        when:
        def response = client.hello()

        then:
        response == "hi"
    }
}