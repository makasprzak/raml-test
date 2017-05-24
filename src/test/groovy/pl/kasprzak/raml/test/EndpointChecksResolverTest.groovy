package pl.kasprzak.raml.test

import org.raml.v2.api.RamlModelBuilder
import org.raml.v2.api.model.v10.api.Api
import spock.lang.Specification

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.expect

class EndpointChecksResolverTest extends Specification {
    def randomizer = { 123 }
    private final EndpointChecksResolver resolver = new EndpointChecksResolver(pathSanitizer: new PathSanitizer(location: 'http://localhost:8080', randomizer: randomizer))

    def "should resolve all endpoint checks"() {
        given:
            def api = buildApi('api.raml')
        when:
            def checks = resolver.resolveEndpointChecksWithApi(api)
        then:
            checks.size() == 3
            def indexed = checks.collectEntries { [[it.method, it.path], (it)] }
            indexed.get(["GET", "http://localhost:8080/user"]).okStatus == 200
            indexed.get(["POST", "http://localhost:8080/user"]).okStatus == 201
            indexed.get(["POST", "http://localhost:8080/user"]).body == '{ "name": "John Bean" }'
            indexed.get(["GET", "http://localhost:8080/user/account"]).okStatus == 200
            indexed.get(["POST", "http://localhost:8080/user"]).responseHeaders == ['Location']
            expect indexed.get(["GET", "http://localhost:8080/user"]).validateResponse('{ "name": "Romeo" }'), empty()
            expect indexed.get(["GET", "http://localhost:8080/user"]).validateResponse('{ "wrong": "Romeo" }'), hasSize( greaterThan(0) )
    }

    def "should replace template uri parameter of unspecified type with dummy string value by default"() {
        given:
            def api = buildApi('default_uri_parameter_example.raml')
        when:
            def checks = resolver.resolveEndpointChecksWithApi(api)
        then:
            checks.size() == 1
            expect checks.first().path, endsWith('/users/some-id')
    }

    def "should replace integer uri parameters with random value by default"() {
        given:
            def api = buildApi('integer_uri_parameter_example.raml')
        when:
            def checks = resolver.resolveEndpointChecksWithApi(api)
        then:
            checks.size() == 1
            expect checks.first().path, endsWith('/users/123')
    }

    def "should replace uri parameters with examples if specified"() {
        given:
            def api = buildApi('provided_uri_parameter_example.raml')
        when:
            def checks = resolver.resolveEndpointChecksWithApi(api)
        then:
            checks.size() == 1
            expect checks.first().path, endsWith('/users/john-bean')
    }

    def "startMark on null object issue"() {
        given:
            Api api = buildApi('startMarkIssue/api.raml')
        when:
            resolver.resolveEndpointChecksWithApi(api)
        then:
            noExceptionThrown()
    }

    private Api buildApi(String path) {
        def raml = readFile path
        def api = new RamlModelBuilder().buildApi(raml, path)
        api.apiV10
    }

    def readFile(String path) {
        getClass().getClassLoader().getResource(path).text
    }

    private Api buildApi(String raml, String path) {
        new RamlModelBuilder().buildApi(raml, path).apiV10
    }

}
