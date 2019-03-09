import com.stehno.ersatz.ErsatzServer
import io.micronaut.http.client.DefaultHttpClient
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.DefaultHttpClient_hack
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultHttpResponse
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.vertx.core.net.ProxyOptions
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.reactivex.ext.web.client.WebClient
import org.littleshoot.proxy.HttpFilters
import org.littleshoot.proxy.HttpFiltersAdapter
import org.littleshoot.proxy.HttpFiltersSourceAdapter
import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.extras.SelfSignedMitmManager
import org.littleshoot.proxy.impl.DefaultHttpProxyServer

import static io.vertx.reactivex.core.Vertx.vertx

String path = "/"

ErsatzServer ersatz = new ErsatzServer({
    https()

    expectations {
        get(path) {
            called 2
            responder {
                body 'Hi!', 'text/plain'
            }
        }
    }
})

ersatz.start()


URL url = new URL(ersatz.httpsUrl + path)

// given a proxy
HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
    .withManInTheMiddle(new SelfSignedMitmManager())
    .withPort(18080)
    .withFiltersSource(new HttpFiltersSourceAdapter() {
        @Override
        HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
            return new HttpFiltersAdapter(originalRequest) {
                @Override
                HttpObject proxyToClientResponse(HttpObject httpObject) {
                    if(httpObject instanceof DefaultHttpResponse) {
                        def response = ((DefaultHttpResponse) httpObject)
                        response.headers().add("proxied", "true")
                        return response
                    } else {
                        return httpObject
                    }
                }
            }
        }
    })
    .start()

InetSocketAddress proxyAddress = new InetSocketAddress("localhost", 18080) // SSLException: handshake timed out

//// Java connection
//
//def proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
//def connection = url.openConnection(proxy)
//assert connection.inputStream.text == "Hi!"
//println "Success with Java URL!!!"

// Vert.x

WebClientOptions options = new WebClientOptions()
    .setDefaultHost(url.host)
    .setDefaultPort(url.port)
    .setTrustAll(true)
    .setSsl(true)
    .setVerifyHost(false)
    .setProxyOptions(new ProxyOptions()
        .setHost(proxyAddress.hostName)
        .setPort(proxyAddress.port)
    )

def vertx = vertx()
def vertxResponse = WebClient.create(vertx, options)
    .get(path)
    .rxSend()
    .blockingGet()

assert vertxResponse.bodyAsString() == "Hi!"
assert vertxResponse.headers().get("proxied") == "true"

println "\n\nSuccess with Vert.x!!!\n\n"

vertx.close()

// Micronaut config

def conf = new DefaultHttpClientConfiguration()
conf.proxyType = Proxy.Type.HTTP
conf.proxyAddress = proxyAddress

// Micronaut (with our hack)

def client = new DefaultHttpClient_hack(url, conf)
def mnResponse = client.exchange(path,String).blockingFirst()

assert mnResponse.getBody().get() == "Hi!"
assert mnResponse.header("proxied") == "true"

println "\n\nSuccess with hacked Micronaut!!!\n\n"

client.close()


// Micronaut (with proxy fix from trunk)

def client2 = new DefaultHttpClient(url, conf)
def mnResponse2 = client2.exchange(path, String).blockingFirst()

assert mnResponse2.getBody().get() == "Hi!"
assert mnResponse2.header("proxied") == "true" // <-- proxy skipped when using ssl

println "\n\nSuccess with Micronaut!!!\n\n"

client2.close()

ersatz.verify()
System.exit(0)