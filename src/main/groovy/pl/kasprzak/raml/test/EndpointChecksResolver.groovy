package pl.kasprzak.raml.test

import groovy.transform.Canonical
import groovy.transform.TupleConstructor
import org.raml.v2.api.RamlModelBuilder
import org.raml.v2.api.RamlModelResult
import org.raml.v2.api.model.v10.api.Api
import org.raml.v2.api.model.v10.bodies.Response
import org.raml.v2.api.model.v10.methods.Method
import org.raml.v2.api.model.v10.resources.Resource

class EndpointChecksResolver {
    private final String location
    EndpointChecksResolver(String location) {
        this.location = location
    }

    List<EndpointCheck> resolveEndpointChecks(raml) {
        Api apiV10 = buildApi(raml)
        List<Method> methods = extract(apiV10.resources()).collectMany { it.methods() }
        List<Tuple2<Method, Response>> responses = methods.collectMany {
            it.responses().collect{ response ->
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

    private void doExtract(Resource resource) {
        def extractedResources = extract(resource.resources())
        if (resource.methods().isEmpty()) extractedResources
        else extractedResources + resource
    }

    private Api buildApi(raml) {
        def api = new RamlModelBuilder().buildApi(raml, location)
        if (api.hasErrors()) throw new RamlParseException(getErrors(api))
        api.apiV10
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
    static class RamlParseException extends RuntimeException {
        List<String> issues

        @Override
        String getMessage() {
            return "following issues found while processing RAML file:\n" + issues.join('\n') + ('\n')
        }
    }
}
