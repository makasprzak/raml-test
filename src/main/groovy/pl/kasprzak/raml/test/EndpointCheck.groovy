package pl.kasprzak.raml.test

import groovy.transform.Canonical
import groovy.transform.TupleConstructor

@Canonical
@TupleConstructor
class EndpointCheck {
    String method
    String path
    Integer okStatus
    def body = ""
    def validationFunction = { it -> Collections.emptyList() }

    List<String> validateResponse(String response) {
        validationFunction(response)
    }
}
