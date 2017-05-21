package pl.kasprzak.raml.test

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.raml.v2.api.RamlModelBuilder
import org.raml.v2.api.model.v10.api.Api
import pl.kasprzak.raml.test.CheckExecutor
import pl.kasprzak.raml.test.EndpointCheck
import pl.kasprzak.raml.test.EndpointChecksResolver
import pl.kasprzak.raml.test.PathSanitizer
import pl.kasprzak.raml.test.RamlParser

class RamlApiCheckTask extends DefaultTask {

    private static final int NOT_TO_GET_TO_FUZZY = 10
    String ramlPath
    int port

    @TaskAction
    def checkApi() {
        def random = new Random()
        def location = "http://localhost:${port}"
        def api = new RamlParser(location: ramlPath).buildApi(readRaml(ramlPath))
        def randomizer = {random.nextInt(NOT_TO_GET_TO_FUZZY)}
        def checks = new EndpointChecksResolver(pathSanitizer: new PathSanitizer(location: location, randomizer: randomizer)).resolveEndpointChecksWithApi(api)
        def executor = new CheckExecutor(port: port)
        checks.forEach { executor.execute(it) }
    }

    def readRaml(String ramlPath) {
        def file = new File(ramlPath)
        if (file.exists()) return file.text
        else return getClass().getResource(ramlPath).text
    }
}
