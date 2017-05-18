package pl.kasprzak.raml.test

import static com.jayway.restassured.RestAssured.given

class CheckExecutor {
    private final int port

    CheckExecutor(port) {
        this.port = port
    }

    def execute(EndpointCheck check) {
        def responseBody = (given().config().port(port).
                when().body(check.body) as MethodExecutor).executeMethod(check.method, check.path)
                .then().statusCode(check.okStatus)
                .extract().response().body().prettyPrint()
        check.validateResponse(responseBody)
        responseBody
    }

    trait MethodExecutor {
        def executeMethod(String method, String path) {
            switch (method) {
                case "GET": return get(path)
                case "POST": return post(path)
                default: throw new RuntimeException("Illegal http method! ${method}")
            }
        }
    }
}
