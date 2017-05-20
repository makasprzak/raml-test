import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.raml.v2.api.RamlModelBuilder
import org.raml.v2.api.model.v10.api.Api
import pl.kasprzak.raml.test.CheckExecutor
import pl.kasprzak.raml.test.EndpointCheck
import pl.kasprzak.raml.test.EndpointChecksResolver
import pl.kasprzak.raml.test.RamlParser

class RamlApiCheckTask extends DefaultTask {

    String ramlPath
    int port

    @TaskAction
    def checkApi() {
        def location = "http://localhost:${port}"
        def api = new RamlParser(location: location).buildApi(readRaml(ramlPath))
        def checks = new EndpointChecksResolver(location: location).resolveEndpointChecksWithApi(api)
        def executor = new CheckExecutor(port: port)
        checks.forEach { executor.execute(it) }
    }

    def readRaml(String ramlPath) {
        def file = new File(ramlPath)
        if (file.exists()) return file.text
        else return getClass().getResource(ramlPath).text
    }
}
