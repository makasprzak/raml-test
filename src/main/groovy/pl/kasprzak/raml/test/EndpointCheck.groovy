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
    Map<String, String> optionalParams
    List<String> responseHeaders
    Closure<List<String>> validateResponse
}
