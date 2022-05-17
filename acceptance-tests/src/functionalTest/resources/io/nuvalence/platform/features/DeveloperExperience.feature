Feature: Developer Experience

  Scenario: service health
    Given the default actuator health endpoint /actuator/health
    When a GET request is made
    Then it should return 200 and contain the text "status":"UP"

  Scenario: swagger documentation
    Given the default swagger spec endpoint /v2/api-docs
    When a GET request is made
    Then it should return 200 and contain the text "swagger":"2.0"

  Scenario: swagger ui
    Given the default swagger ui endpoint /swagger-ui.html
    When a GET request is made
    Then it should return 200 and contain the text <title>Swagger UI</title>