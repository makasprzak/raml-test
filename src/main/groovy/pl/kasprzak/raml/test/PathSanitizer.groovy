package pl.kasprzak.raml.test

import org.raml.v2.api.model.v10.datamodel.ExampleSpec
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration

class PathSanitizer {
    def location
    def randomizer

    String sanitizePath(String path, Map<String, TypeDeclaration> uriParameterTypes) {
        location + path.replaceAll(/\{(id)}/) { all, template -> replace(template, uriParameterTypes) }
    }

    private String replace(template, Map<String, TypeDeclaration> uriParameterTypes) {
        if (uriParameterTypes.containsKey(template)) {
            def optionalExample = getExample(uriParameterTypes, template)
            if (optionalExample.isPresent()) {
                return optionalExample.get().value()
            } else if (isInteger(uriParameterTypes, template)) {
                return randomizer()
            } else return "some-${template}"
        } else return "some-${template}"
    }

    private static boolean isInteger(Map<String, TypeDeclaration> uriParameterTypes, template) {
        Optional.ofNullable(uriParameterTypes.get(template)).filter { it.type() == 'integer' }.isPresent()
    }

    private static Optional<ExampleSpec> getExample(Map<String, TypeDeclaration> uriParameterTypes, template) {
        Optional.ofNullable(uriParameterTypes.get(template).example()).filter { !it.value().isEmpty() }
    }

}
