package pl.kasprzak.raml.test

class PathSanitizer {
    def location

    String sanitizePath(String path) {
        location + path.replaceAll(/\{(id)}/){ all, template -> "some-${template}" }
    }

}
