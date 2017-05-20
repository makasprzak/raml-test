package pl.kasprzak.raml.test

import groovy.transform.TupleConstructor

class PathSanitizer {
    def location
    def randomizer

    String sanitizePath(String path, Map<String, ParameterSpec> uriParameterTypes) {
        location + path.replaceAll(/\{(id)}/) { all, template -> replace(template, uriParameterTypes) }
    }

    private String replace(template, Map<String, ParameterSpec> uriParameterTypes) {
        if (uriParameterTypes.containsKey(template)) {
            def optionalExample = uriParameterTypes.get(template).example
            if (optionalExample.isPresent()) {
                return optionalExample.get()
            } else if (isInteger(uriParameterTypes, template)) {
                return randomizer()
            } else return "some-${template}"
        } else return "some-${template}"
    }

    private static boolean isInteger(Map<String, ParameterSpec> uriParameterTypes, template) {
        Optional.ofNullable(uriParameterTypes.get(template)).filter { it.type == 'integer' }.isPresent()
    }

    @TupleConstructor
    static class ParameterSpec {
        String type
        Optional<String> example
    }
}
