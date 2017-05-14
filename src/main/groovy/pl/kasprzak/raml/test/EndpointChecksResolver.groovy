package pl.kasprzak.raml.test

import groovy.transform.Canonical
import groovy.transform.TupleConstructor
import org.raml.v2.api.RamlModelBuilder
import org.raml.v2.api.RamlModelResult
import org.raml.v2.api.model.v10.api.Api
import org.raml.v2.api.model.v10.methods.Method
import org.raml.v2.api.model.v10.bodies.Response
import org.raml.v2.api.model.v10.resources.Resource

class EndpointChecksResolver {
    private final String location
    EndpointChecksResolver(String location) {
        this.location = location
    }

    List resolveEndpointChecks(raml) {
        Api apiV10 = buildApi(raml)
        def resources = findAllResources(apiV10)
        List<Method> methods = resources.collectMany { it.methods() }
        List<Tuple2<Method, Response>> responses = methods.collectMany {
            it.responses().collect{ response ->
                new Tuple2<>(it, response)
            }
        }
        responses.collect { methodAndResponse ->
            check(methodAndResponse.first, methodAndResponse.first.resource(), methodAndResponse.second)
        }
    }

    private List<Resource> findAllResources(Api apiV10) {
        extract(apiV10.resources())
    }

    private List<Resource> extract(List<Resource> resources) {
        if (resources.isEmpty()) Collections.emptyList()
        else resources.collectMany { resource ->
            if (resource.methods().isEmpty()) extract(resource.resources())
            else extract(resource.resources()) + resource
        }
    }

    private Api buildApi(raml) {
        def api = new RamlModelBuilder().buildApi(raml, location)
        if (api.hasErrors()) throw new EndpointResolutionException(getErrors(api))
        def apiV10 = api.apiV10
        apiV10
    }

    private List<String> getErrors(RamlModelResult api) {
        api.getValidationResults()*.getMessage().collect()
    }

    private EndpointCheck check(Method method, Resource resource, Response response) {
        new EndpointCheck(
                method: method.method().toUpperCase(),
                path: location + resource.resourcePath(),
                okStatus: response.code().value().toInteger())
    }

    @Canonical
    @TupleConstructor
    static class EndpointCheck {
        String method
        String path
        Integer okStatus
    }

    @Canonical
    @TupleConstructor
    static class EndpointResolutionException extends RuntimeException {
        List<String> issues

        @Override
        String getMessage() {
            return "following issues found while processing RAML file:\n" + issues.join('\n') + ('\n')
        }
    }
}
