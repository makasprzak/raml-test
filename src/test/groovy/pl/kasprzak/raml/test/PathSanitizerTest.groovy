package pl.kasprzak.raml.test

import spock.lang.Specification

class PathSanitizerTest extends Specification {
    def "should replace template with dummy value"() {
        expect: new PathSanitizer(location: '..').sanitizePath('/users/{id}') == '../users/some-id'
    }
}
