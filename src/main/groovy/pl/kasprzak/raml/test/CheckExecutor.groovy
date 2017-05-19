package pl.kasprzak.raml.test

import com.jayway.restassured.specification.RequestSpecification

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

    trait MethodExecutor implements RequestSpecification {
        def executeMethod(String method, String path) {
            switch (method) {
                case "GET": return get(path)
                case "POST": return post(path)
                case "PUT": return  put(path)
                default: throw new IllegalArgumentException("Illegal http method! ${method}")
            }
        }
    }
}
