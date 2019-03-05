import io.micronaut.http.client.DefaultHttpClient
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.vertx.core.net.ProxyOptions
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.reactivex.ext.web.client.WebClient
import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.impl.DefaultHttpProxyServer

import static io.vertx.reactivex.core.Vertx.vertx

String url = "https://www.google.com"
String path = "/"

// given a proxy
HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
    .withPort(18080)
    .start()

InetSocketAddress proxyAddress = new InetSocketAddress("localhost", 18080) // SSLException: handshake timed out

// Java connection

def proxy = new Proxy(Proxy.Type.HTTP, proxyAddress)
def connection = new URL(url + path).openConnection(proxy)
connection.inputStream.text
println "Success with Java URL!!!"

// Vert.x

WebClientOptions options = new WebClientOptions()
    .setDefaultHost(url)
    .setProxyOptions(new ProxyOptions()
    .setHost(proxyAddress.getHostName())
    .setPort(proxyAddress.getPort())
)

WebClient.create(vertx(), options)
    .get(path)
    .rxSend()
    .blockingGet()

println "Success with Vert.x!!!"

// Micronaut

def conf = new DefaultHttpClientConfiguration()
conf.proxyType = Proxy.Type.HTTP
conf.proxyAddress = proxyAddress
def client = new DefaultHttpClient(new URL(url), conf)
client.retrieve(path).blockingFirst() // <<-- throws exception

println "Success with Micronaut!!!"