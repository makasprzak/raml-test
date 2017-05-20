package pl.kasprzak.raml.test

class PathSanitizer {
    def location
    def randomizer

    String sanitizePath(String path, Map<String, String> uriParameterTypes) {
        location + path.replaceAll(/\{(id)}/){ all, template -> replace(template, uriParameterTypes) }
    }

    private String replace(template, Map<String, String> uriParameterTypes) {
        if (uriParameterTypes.containsKey(template) && uriParameterTypes.get(template) == 'integer') {
            return randomizer()
        }
        else return "some-${template}"
    }

}
