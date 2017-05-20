package pl.kasprzak.raml.test

import com.jayway.restassured.response.ResponseOptions
import com.jayway.restassured.specification.RequestSpecification

import static com.jayway.restassured.RestAssured.given

class CheckExecutor {
    def port

    def execute(EndpointCheck check) {
        try {
            doExecute(check)
        } catch (ConnectException e) {
            throw new AssertionError("Could not connect to backend under test, is the server up?", e)
        }
    }

    private void doExecute(EndpointCheck check) {
        def response = given().log().all().config().port(port).
                when()
                .body(check.body)
                .contentType("application/json")
                .withTraits(MethodExecutor).executeMethod(check.method, check.path)
                .then()
                .statusCode(check.okStatus)
                .extract().response().withTraits(HeaderAssert, ResponseValidator)
        response.assertHeaders check.responseHeaders
        response.validateResponse check.validateResponse
    }

    trait HeaderAssert implements ResponseOptions {
        def assertHeaders(List<String> headers) {
            Optional.ofNullable(headers).filter{!it.isEmpty()}.ifPresent({
                assert getHeaders().asList().collect {it.getName()}.containsAll(headers)
            })
        }
    }

    trait ResponseValidator implements ResponseOptions {
        def validateResponse(Closure<List<String>> validationFunction) {
            assert validationFunction(body().prettyPrint()).size() == 0
        }
    }

    trait MethodExecutor implements RequestSpecification {
        def executeMethod(String method, String path) {
            switch (method) {
                case "GET": return get(path)
                case "POST": return post(path)
                case "PUT": return  put(path)
                case "DELETE": return delete(path)
                default: throw new IllegalArgumentException("Unsupported http method! ${method}")
            }
        }
    }
}
