package pl.kasprzak.raml.test

import spock.lang.Specification

import static org.hamcrest.Matchers.containsInAnyOrder
import static spock.util.matcher.HamcrestSupport.expect

class EndpointChecksResolverTest extends Specification {
    def location = 'http://localhost:8080'
    private final EndpointChecksResolver resolver = new EndpointChecksResolver(location)

    def "should resolve all endpoint checks"() {
        def raml = """#%RAML 1.0
title: test-api
types:
  User:
    type: object
    properties:
      name: string
  Account:
    type: object
    properties:
      id: number    
/user:
  get:
    responses:
      200:
        body:
          application/json:
            type: User
  post:
    body:
      application/json:
        example: { "name": "John Bean" }
    responses:
      201:
        headers:
          Location:
            example: http://localhost:8080/user
  /account:
    get:
      responses:
        200:
          body:
            application/json:
              type: Account
                            
                 
"""
        when:
            def checks = resolver.resolveEndpointChecks(raml)
        then:
            checks.size() == 3
            expect checks, containsInAnyOrder(new EndpointChecksResolver.EndpointCheck(method: "GET", path: "http://localhost:8080/user", okStatus: 200),
                   new EndpointChecksResolver.EndpointCheck(method: "POST", path: "http://localhost:8080/user", okStatus: 201),
                   new EndpointChecksResolver.EndpointCheck(method: "GET", path: "http://localhost:8080/user/account", okStatus: 200))
    }

    def "should report raml errors"() {
        when:
        resolver.resolveEndpointChecks("")

        then:
        EndpointChecksResolver.EndpointResolutionException ex = thrown()
        ex.issues.head().contains("Empty document")
    }
}
