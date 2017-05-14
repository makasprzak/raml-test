import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.raml.v2.api.RamlModelBuilder

class RamlApiCheckTask extends DefaultTask {
    
    String ramlPath = 'api.raml'
    
    @TaskAction
    def checkApi() {
        def modelResult = new RamlModelBuilder().buildApi(ramlPath)
        if (modelResult.hasErrors()) for (r in modelResult.getValidationResults()) {
            println(r.message)
        } else {
            def api = modelResult.apiV10
            api.resources().each{resource ->
                println resource.resourcePath()
            }
        }
    }
}
