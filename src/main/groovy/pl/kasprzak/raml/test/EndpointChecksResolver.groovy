package pl.kasprzak.raml.test

import org.raml.v2.api.model.v10.api.Api
import org.raml.v2.api.model.v10.bodies.Response
import org.raml.v2.api.model.v10.datamodel.ExampleSpec
import org.raml.v2.api.model.v10.methods.Method
import org.raml.v2.api.model.v10.resources.Resource

class EndpointChecksResolver {
    PathSanitizer pathSanitizer

    List<EndpointCheck> resolveEndpointChecksWithApi(Api apiV10) {
        List<Method> methods = extract(apiV10.resources()).collectMany { it.methods() }
        List<Tuple2<Method, Response>> responses = methods.collectMany {
            it.responses().collect { response ->
                new Tuple2<>(it, response)
            }
        }
        responses.collect { methodAndResponse ->
            check(methodAndResponse.first, methodAndResponse.first.resource(), methodAndResponse.second)
        }
    }

    private List<Resource> extract(List<Resource> resources) {
        if (resources.isEmpty()) Collections.emptyList()
        else resources.collectMany { doExtract it }
    }

    private List<Resource> doExtract(Resource resource) {
        def extractedResources = extract(resource.resources())
        if (resource.methods().isEmpty()) extractedResources
        else extractedResources + resource
    }

    private EndpointCheck check(Method method, Resource resource, Response response) {
        EndpointCheck check = new EndpointCheck(
                method: method.method().toUpperCase(),
                path: buildPath(resource),
                okStatus: response.code().value().toInteger(),
                validateResponse: responseBodyValidationClosure(response),
                responseHeaders: response.headers()*.name().collect()
        )
        extractBody(method).ifPresent{check.body = it}
        return check
    }

    private String buildPath(Resource resource) {
        def parameterTypes = resource.uriParameters().collectEntries {
            [(it.name()): [type: it.type(), example: Optional.ofNullable(it.example()).map{it.value()}]]
        }
        pathSanitizer.sanitizePath(resource.resourcePath(), parameterTypes)
    }


    private static Closure<List> responseBodyValidationClosure(response) {
        { String payload -> response.body()*.validate(payload).collectMany { it.message } }
    }

    private static Optional<String> extractBody(Method method) {
        Optional.of(method).map { it.body() }
                .filter { !it.isEmpty() }
                .map { it.first() }
                .map { it.example() }
                .map { extractOriginalValue_dirtyHack(it) }
    }

    private static String extractOriginalValue_dirtyHack(ExampleSpec it) {
        def node = it.node.yamlNode
        def startMark = node.startMark
        def endMark = node.endMark
        endMark.buffer.substring(startMark.index, endMark.index)
    }

}
