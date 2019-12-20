package custom.discovery.gateway

import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Shared
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DiscoveryNotRefreshedDemonstrationTest extends Specification {

    @Shared
    def SERVICE_PORT_0 = 8081
    @Shared
    def SERVICE_PORT_1 = 8082
    @Shared
    def SERVICE_PORT_2 = 8083
    @Shared
    def ITERATIONS = 40

    private WireMockServer wireMockServer0 = new WireMockServer(SERVICE_PORT_0)
    private WireMockServer wireMockServer1 = new WireMockServer(SERVICE_PORT_1)
    private WireMockServer wireMockServer2 = new WireMockServer(SERVICE_PORT_2)

    @LocalServerPort
    protected int gatewayPort = 0

    @Autowired
    private WebTestClient testClient

    def setup() {
        wireMockServer0.start()
        wireMockServer1.start()
        wireMockServer2.start()
        testClient.bindToServer().baseUrl("http://localhost:" + gatewayPort).build()
    }

    def cleanup() {
        wireMockServer0.stop()
        wireMockServer1.stop()
        wireMockServer2.stop()
    }

    def "multiple GET requests are sent to different backends"() {
        given: 'a running backend service'
        configureGET("/")
        def responses = []
        when: 'GET requests are sent'
        for (int i = 0; i < ITERATIONS; i++) {
            responses << testClient.get().uri("/service/").exchange()
        }
        then: 'response is ok'
        responses.each { assert it.expectStatus().isOk() }
        and: 'backend services where called' // fails, because discovery data is not refreshed
        wireMockServer0.verify(getRequestedFor(urlPathEqualTo("/")))
        wireMockServer1.verify(getRequestedFor(urlPathEqualTo("/")))
        wireMockServer2.verify(getRequestedFor(urlPathEqualTo("/"))) // <-- This is the only backend server being called
    }

    private configureGET(String url) {
        wireMockServer0.stubFor(get(urlEqualTo(url))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()))
        )
        wireMockServer1.stubFor(get(urlEqualTo(url))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()))
        )
        wireMockServer2.stubFor(get(urlEqualTo(url))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value()))
        )
    }
}
