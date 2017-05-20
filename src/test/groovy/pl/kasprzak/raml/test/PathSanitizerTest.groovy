package pl.kasprzak.raml.test

import spock.lang.Specification

class PathSanitizerTest extends Specification {
    private PathSanitizer sanitizer = new PathSanitizer(location: '..', randomizer: { 321 })

    def "should replace template with dummy value by default"() {
        expect:
        sanitizer.sanitizePath('/users/{id}', [:]) == '../users/some-id'
    }

    def "should replace template respecting specified type"() {
        expect:
        sanitizer.sanitizePath('/users/{id}', [id: [type: 'integer', example: Optional.empty()]]) == '../users/321'
    }
}
