package pl.kasprzak.raml.test

import groovy.transform.Canonical
import groovy.transform.TupleConstructor
import org.raml.v2.api.RamlModelBuilder
import org.raml.v2.api.RamlModelResult
import org.raml.v2.api.model.v10.api.Api

class RamlParser {

    def location

    Api buildApi(raml) {
        def api = new RamlModelBuilder().buildApi(raml, location)
        if (api.hasErrors()) throw new RamlParseException(getErrors(api))
        return api.apiV10
    }

    private List<String> getErrors(RamlModelResult api) {
        api.getValidationResults()*.getMessage().collect()
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
