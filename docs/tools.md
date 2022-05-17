# Project Tools and Frameworks
Reference links and some justification for usage of various tools
and frameworks. Contributors should feel free to add to this 
document with any additional learning materials they've found useful. 

## Build Tools

### Gradle | [docs](https://docs.gradle.org/current/userguide/userguide.html)
Gradle allows you to define build logic as code, using many of the same
conventions established by Maven. If you haven't used Gradle before, read 
the [CLI documentation](https://docs.gradle.org/current/userguide/command_line_interface.html) 
for explanations of build commands you'll use day to day.

### JIB | [docs](https://github.com/GoogleContainerTools/jib)
Builds optimized container images for java applications.

## Spring
Spring is a popular framework for rapid Java application development.

### Spring Boot | [docs](https://spring.io/projects/spring-boot)
Spring Boot packages your application with an embedded Tomcat server
into a single executable jar for simple creation of standalone web services.

All you have to do is run `./gradlew bootJar` to have a service up and running.

The service module uses the spring Gradle plugin to provide standard tasks
and configuration, and the spring dependency management plugin configures a BOM
with reference versions for libraries commonly used with Spring applications.

If you've not used Spring Boot before, this 
[developer guide](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using)
provides information that will help to understand the ecosystem and concepts. Some specific sections
of this guide are also called out below.

### Spring Starters | [docs](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.build-systems.starters)
Spring provides much of its rapid development functionality bundled as "starters". 
This page describes the list of functionality spring provides for different use cases. 

### DevTools | [docs](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools)
Provides support for remote debugging and live reloading. 
[This blog](https://www.baeldung.com/spring-boot-devtools) explains the features in a bit more depth
than the Spring documentation. 

### Spring Actuator | [docs](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
Provides a collection of production-readiness capabilities (eg: health endpoint, metrics).

## Code Generation

### Open API Generator | [docs](https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin)
We've chosen an API first approach to enable us to generate our service controllers, 
client, and models from a predefined spec. This Gradle plugin is applied and configured at
[gradle/openapi-tools](../gradle/openapi-tools) to generate Java source code from a 
[swagger doc](../swagger.yaml). The code is generated to the project's 
`build/generate-resources` directory, and included as a sourceSet in the
build (so it is compiled into the jar).

### Lombok | [docs](https://projectlombok.org/)
Compile time annotations and code generation to reduce boilerplate code.

Some common annotations you'll want to become familiar with:
 - [@Data](https://projectlombok.org/features/Data), plus the annotations it encapsulates
 - [@Builder](https://projectlombok.org/features/Builder)
 - [@NoArgsConstructor, @AllArgsConstructor, @RequiredArgsConstructor](https://projectlombok.org/features/constructor)

### MapStruct | [docs](https://mapstruct.org/)
Convention-based code generator for mapping/translating objects between types. 

## Code Quality
A number of tools are applied to all submodules in this repository via the build script
located at [gradle/code-quality.gradle](../gradle/code-quality.gradle). These tools are
intended to improve quality, save time in code reviews, and provide clarity on project 
standards for new contributors. 

**When any of these fail, you can find reports in the module's `build/reports` directory.**

### Checkstyle | [docs](https://checkstyle.sourceforge.io/)
Allows configuration of project standards inclusive of
 - style/formatting
 - code complexity rules
 - documentation requirements

Each project will have a `checkstyleMain` and `checkstyleTest` task for running these checks.\

#### Configure Checkstyle in IntelliJ
1. Install the Checkstyle plugin
2. Set Checkstyle version to 8.25 in IntelliJ Preferences under Tools/Checkstyle
3. Import Checkstyle config file from `config/checkstyle/checkstyle.xml`

### Spotless | [docs](https://github.com/diffplug/spotless)
Code formatter & validator. This tool was pulled in primarily for developer convenience, 
to give a way to auto resolve some formatting related checkstyle violations. 

To auto-format source code, run: `./gradlew spotlessApply`

### PMD | [docs](https://pmd.github.io/pmd-6.41.0/)
Scans source code for common bugs.

Each project will have a `pmdMain` and `pmdTest` task for running these checks.

### SpotBugs | [docs](https://spotbugs.readthedocs.io/en/stable/)
Scans byte code (compiled java) for common bugs.

Each project will have a `spotbugsMain` and `spotbugsTest` task for running these checks.

### Jacoco | [docs](https://www.jacoco.org/index.html)
Generates code coverage reports and verifies coverage thresholds. The default threshold is set
to 90% instructions covered throughout the module. 

Coverage thresholds are verified every time you run tests. 

You can run `./gradlew jacocoTestReport` to get a full html report with detailed coverage data
on every class and method. 

## Testing
Testing frameworks used by the unit and/or acceptance tests. This Nuvalence 
[blog post](https://nuvalence.io/blog/getting-started-with-automated-testing) 
discusses some testing best practices and links to tutorials for those new to testing. 

### JUnit | [docs](https://junit.org/junit5/)
A common Java testing framework - provides annotations for configuring
the test runner, also has some out of the box assertions.

This project specifically uses JUnit 5 "Jupiter". 

### Hamcrest | [docs](http://hamcrest.org/JavaHamcrest/tutorial)
While JUnit provides some standard assertions, sometimes you will want
to verify that an object matches expectations in some way other than 
strict equality. Hamcrest provides some common matchers and lets you
implement custom matchers to be used in test assertions. 

### Cucumber | [docs](https://cucumber.io/docs/cucumber/)
Behavior Driven Development framework that links human-readable, gherkin
format test cases to code and assertions. A sample spec can be seen in the 
[acceptance-tests resources](../acceptance-tests/src/functionalTest/resources/io/nuvalence/platform/features/DeveloperExperience.feature).
Its step definitions (underlying code) can be found in the 
[acceptance-tests source](../acceptance-tests/src/functionalTest/java/io/nuvalence/platform/DeveloperExperienceStepDefinitions.java).

These tests ultimately still get run as JUnit tests. 

## Continuous Integration & Deployment
Tools used for managing pipelines and infrastructure. 

### Cloud Build | [docs](https://cloud.google.com/build/docs/overview)
GCP native build pipelines. 

### Terraform | [docs](https://learn.hashicorp.com/terraform?utm_source=terraform_io&utm_content=terraform_io_hero)
Infrastructure as code; maintains state and performs updates. 
