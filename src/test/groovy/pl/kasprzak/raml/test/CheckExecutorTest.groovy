package pl.kasprzak.raml.test

import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.socket.PortFactory
import spock.lang.Specification

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

    def "should fail for wrong status"() {
        given:

        server
            .when(
                HttpRequest.request()
                    .withMethod("GET")
                    .withPath("/user")
        )
        .respond(
            HttpResponse.response()
                .withStatusCode(404)
        )

        def check = new EndpointCheck(method: "GET", path: "/user", okStatus: 200, validateResponse: {Collections.emptyList()})

        when:
        executor.execute(check)

        then:
        thrown AssertionError
    }

    def "should fail for wrong method"() {
        given:

        def check = new EndpointCheck(method: "BAD", path: "/user", okStatus: 200, validateResponse: {Collections.emptyList()})

        when:
        executor.execute(check)

        then:
        thrown IllegalArgumentException
    }

    def "should check post request"() {
        given:

        server
            .when(
                HttpRequest.request()
                    .withMethod("POST")
                    .withPath("/user")
                    .withBody("""{"name":"John Bean"}""")
        )
        .respond(
            HttpResponse.response()
                .withStatusCode(201)
                .withHeader("Content-Type", "application/json")
        )
        def invocations = new HashSet<>()
        Closure<List<String>> validationFunction = { String body ->
            invocations.add(body)
            return Collections.emptyList()
        }
        def check = new EndpointCheck(
                method: "POST",
                path: "/user",
                body: """{"name":"John Bean"}""",
                okStatus: 201,
                validateResponse: validationFunction)

        when:
        executor.execute(check)

        then:
        expect invocations, hasSize(1)
    }
    def "should check put request"() {
        given:

        server
            .when(
                HttpRequest.request()
                    .withMethod("PUT")
                    .withPath("/user")
        )
        .respond(
            HttpResponse.response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
        )
        def check = new EndpointCheck(
                method: "PUT",
                path: "/user",
                body: """{"name":"John Bean"}""",
                okStatus: 200,
                validateResponse: {Collections.emptyList()})

        when:
        executor.execute(check)

        then:
        noExceptionThrown()
    }
    def "should check delete request"() {
        given:

        server
            .when(
                HttpRequest.request()
                    .withMethod("DELETE")
                    .withPath("/user")
        )
        .respond(
            HttpResponse.response()
                .withStatusCode(200)
        )
        def check = new EndpointCheck(
                method: "DELETE",
                path: "/user",
                okStatus: 200,
                validateResponse: {Collections.emptyList()})

        when:
        executor.execute(check)

        then:
        noExceptionThrown()
    }


}
