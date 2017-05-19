package pl.kasprzak.raml.test

import spock.lang.Specification

import static org.hamcrest.Matchers.hasSize
import static spock.util.matcher.HamcrestSupport.expect

class RamlParserTest extends Specification {
    def parser = new RamlParser(location: 'http://localhost:8080')

    def "should report raml errors"() {
        when:
        parser.buildApi("")

        then:
        RamlParser.RamlParseException ex = thrown()
        ex.issues.head().contains("Empty document")
    }

    def "should parse successfully"() {
        when:
        def api = parser.buildApi(getClass().getResource('/api.raml').text)

        then:
        expect api.resources(), hasSize(1)
    }

}
