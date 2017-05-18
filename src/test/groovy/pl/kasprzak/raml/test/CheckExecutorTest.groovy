package pl.kasprzak.raml.test

import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.socket.PortFactory
import spock.lang.Specification

import static com.jayway.restassured.RestAssured.given
import static org.hamcrest.Matchers.hasSize
import static spock.util.matcher.HamcrestSupport.expect

class CheckExecutorTest extends Specification {
    def server
    def static port = PortFactory.findFreePort()
    private CheckExecutor executor = new CheckExecutor(port)

    def setup() {
        server = ClientAndServer.startClientAndServer(port)
    }

    def cleanup() {
        server.stop()
    }

    def "should check get request"() {
        given:

        server
            .when(
                HttpRequest.request()
                    .withMethod("GET")
                    .withPath("/user")
        )
        .respond(
            HttpResponse.response()
                .withStatusCode(200)
                .withBody("""{"name":"John Bean"}""")
                .withHeader("Content-Type", "application/json")
        )
        def invocations = new HashSet<>()
        Closure<List<String>> validationFunction = { String body ->
            invocations.add(body)
            return Collections.emptyList()
        }
        def check = new EndpointCheck(method: "GET", path: "/user", okStatus: 200, validateResponse: validationFunction)

        when:
        executor.execute(check)

        then:
        expect invocations, hasSize(1)
    }

    def "should check post request"() {
        given:

        server
            .when(
                HttpRequest.request()
                    .withMethod("POST")
                    .withPath("/user")
        )
        .respond(
            HttpResponse.response()
                .withStatusCode(201)
                .withBody("""{"name":"John Bean"}""")
                .withHeader("Content-Type", "application/json")
        )
        def invocations = new HashSet<>()
        Closure<List<String>> validationFunction = { String body ->
            invocations.add(body)
            return Collections.emptyList()
        }
        def check = new EndpointCheck(method: "POST", path: "/user", okStatus: 201, validateResponse: validationFunction)

        when:
        executor.execute(check)

        then:
        expect invocations, hasSize(1)
    }


}
