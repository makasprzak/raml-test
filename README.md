# raml-test
Gradle Task that verifies RESTful API contract against RAML file 

[![Build Status](https://travis-ci.org/makasprzak/raml-test.svg?branch=master)](https://travis-ci.org/makasprzak/raml-test)
[![Coverage Status](https://coveralls.io/repos/github/makasprzak/raml-test/badge.svg?branch=master&dummy)](https://coveralls.io/github/makasprzak/raml-test?branch=master)

### what is it for
The task is meant to verify that your RAML spec is not getting outdated, rather than trying to comprehensively test your API against it. As wise man said - _outdated documentation is worse than no documentation_.

### how to use it
```groovy
task testRaml(type: pl.kasprzak.raml.test.RamlApiCheckTask) {
    ramlPath = '../api/my-api.raml' //a path to the raml file relative to the project directory
    port = 8080 //the port on which your server under test is listening
}

check.dependsOn testRaml
```
For example you can use docker-compose with [docker-compose-gradle-plugin](https://github.com/avast/docker-compose-gradle-plugin) to start your application before verifying the RAML spec and then stopping it after the test is done.
```groovy
testRaml.dependsOn composeUp
testRaml.finalizedBy composeDown
```

### how it works
Simply. The task executes request for every resource and method it finds in a raml file. For POST requests it uses an example json you provide in the RAML (therefore the examples are a must have). To validate the response, the validation function provided by [raml-java-parser](https://github.com/raml-org/raml-java-parser) is used.
