package pl.kasprzak.raml.test

import spock.lang.Specification

import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.empty
import static org.hamcrest.Matchers.greaterThan
import static org.hamcrest.Matchers.hasSize
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
        type: object
        example: { \"name\": \"John Bean\" }
    responses:
      201:
        responseHeaders:
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
            def indexed = checks.collectEntries { [new Tuple2<>(it.method, it.path), (it)] }
            indexed.get(new Tuple2<>("GET", "http://localhost:8080/user")).okStatus == 200
            indexed.get(new Tuple2<>("POST", "http://localhost:8080/user")).okStatus == 201
            indexed.get(new Tuple2<>("POST", "http://localhost:8080/user")).body == '{ "name": "John Bean" }'
            indexed.get(new Tuple2<>("GET", "http://localhost:8080/user/account")).okStatus == 200
            expect indexed.get(new Tuple2<>("GET", "http://localhost:8080/user")).validateResponse('{ "name": "Romeo" }'), empty()
            expect indexed.get(new Tuple2<>("GET", "http://localhost:8080/user")).validateResponse('{ "wrong": "Romeo" }'), hasSize( greaterThan(0) )
    }

    def "should report raml errors"() {
        when:
        resolver.resolveEndpointChecks("")

        then:
        EndpointChecksResolver.RamlParseException ex = thrown()
        ex.issues.head().contains("Empty document")
    }
}
