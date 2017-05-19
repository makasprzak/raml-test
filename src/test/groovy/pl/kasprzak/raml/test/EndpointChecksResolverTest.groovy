package pl.kasprzak.raml.test

import org.raml.v2.api.RamlModelBuilder
import org.raml.v2.api.model.v10.api.Api
import spock.lang.Specification

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.expect

class EndpointChecksResolverTest extends Specification {
    def location = 'http://localhost:8080'
    private final EndpointChecksResolver resolver = new EndpointChecksResolver(location: location)

    def "should resolve all endpoint checks"() {
        String raml = getClass().getResource('/api.raml').text
        when:
            def checks = resolver.resolveEndpointChecksWithApi(buildApi(raml))
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

    private Api buildApi(String raml) {
        new RamlModelBuilder().buildApi(raml, location).apiV10
    }

}
