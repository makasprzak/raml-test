package pl.kasprzak.raml.test

import spock.lang.Specification

import static org.hamcrest.Matchers.hasSize
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response
import static spock.util.matcher.HamcrestSupport.expect

class CheckExecutorTest extends Specification implements MockServerTestBase {
    private CheckExecutor executor = new CheckExecutor(port: randomPort)

    def "should check get request"() {
        given:

        server
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/user")
        )
        .respond(
            response()
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

    def "should throw AssertionError to communicate bad response body"() {
        given:

        server
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/user")
        )
        .respond(
            response()
                .withStatusCode(200)
                .withBody("""{"bad":"John Bean"}""")
                .withHeader("Content-Type", "application/json")
        )
        def check = new EndpointCheck(method: "GET", path: "/user", okStatus: 200, validateResponse: { String body -> Collections.singletonList("bad body") })

        when:
        executor.execute(check)

        then:
        thrown AssertionError
    }

    def "should fail for wrong status"() {
        given:

        server
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/user")
        )
        .respond(
            response()
                .withStatusCode(404)
        )

        def check = new EndpointCheck(method: "GET", path: "/user", okStatus: 200, validateResponse: {Collections.emptyList()})

        when:
        executor.execute(check)

        then:
        thrown AssertionError
    }

    def "should fail for wrong response header"() {
        given:

        server
            .when(
                request()
                    .withMethod("POST")
                    .withPath("/user")
                    .withBody("""{"name":"John Bean"}""")
        )
        .respond(
            response()
                .withStatusCode(201)
                .withHeader("Not-A-Location", "/user")
        )

        def check = new EndpointCheck(method: "POST", path: "/user", body: """{"name":"John Bean"}""", okStatus: 201, responseHeaders: ['Location'], validateResponse: {Collections.emptyList()})

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
                request()
                    .withMethod("POST")
                    .withPath("/user")
                    .withBody("""{"name":"John Bean"}""")
        )
        .respond(
            response()
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
                request()
                    .withMethod("PUT")
                    .withPath("/user")
        )
        .respond(
            response()
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
                request()
                    .withMethod("DELETE")
                    .withPath("/user")
        )
        .respond(
            response()
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
