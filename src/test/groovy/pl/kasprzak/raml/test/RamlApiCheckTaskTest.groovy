package pl.kasprzak.raml.test

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.mockserver.socket.PortFactory
import spock.lang.Specification

import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response
import static org.mockserver.model.JsonBody.json

class RamlApiCheckTaskTest extends Specification implements MockServerTestBase {

    def "should pass for valid contract implementation"() {
        given:
        def task = createTask()
        server
                .when(
                request()
                        .withMethod("GET")
                        .withPath("/user")
        )
                .respond(
                response()
                        .withStatusCode(200)
                        .withBody('{"name":"John Bean"}')
                        .withHeader("Content-Type", "application/json")
        )
        server.when(
                request()
                        .withMethod("POST")
                        .withPath("/user")
                        .withBody(json('{"name":"John Bean"}'))
        )
                .respond(
                response()
                        .withStatusCode(201)
                        .withHeader("Location", "whatever")

        )
        server.when(
                request()
                        .withMethod("GET")
                        .withPath("/user/account")
        )
                .respond(
                response()
                        .withStatusCode(200)
                        .withBody('{"id":123}')
                        .withHeader("Content-Type", "application/json")
        )

        when:
        task.checkApi()

        then:
        noExceptionThrown()
    }

    def "should fail for broken api contract"() {
        when:
        createTask().checkApi()
        then:
        thrown AssertionError
    }

    def "should notify of no server running"() {
        given:
            RamlApiCheckTask task = createRamlTaskFor '/api.raml'
        when:
            task.checkApi()
        then:
            thrown AssertionError
    }

    private RamlApiCheckTask createTask() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('checkApi', type: RamlApiCheckTask) {
            ramlPath = '/api.raml'
            port = randomPort
        }
        return task
    }

    private RamlApiCheckTask createRamlTaskFor(ramlExample) {
        Project project = ProjectBuilder.builder().build()
        RamlApiCheckTask task = project.task('checkApi', type: RamlApiCheckTask) {
            ramlPath = ramlExample
            port = PortFactory.findFreePort()
        }
        task
    }
}
