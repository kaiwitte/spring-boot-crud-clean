The year is 2025.

This is a CRUD project with the following purposes:

- Train and, if successful, demonstrate best practices to any interested party for the
  methods and technologies, as well as their advantages and limitations:
    - Spring Boot
    - OpenAPI with generator
- Practice and try out a very conservative approach to AI tools such as
  Claude, GitHub Copilot and Cursor.

## Code style

- all local variables final

## Architeture, methods and technologies

- OpenAPI generator
    - a typical endpoint for an entity looks like this in the OpenAPI spec. Note the use of GET/POST/PUT/DELETE:
      ```yaml
        /[entitynamePlural]:
          post:
            tags:
              - [entityname]
            summary: Add [Entityname]
            operationId: new[Entityname]
            requestBody:
              required: true
              content:
                application/json:
                  schema:
                    $ref: '#/components/schemas/[Entityname]Request'
            responses:
              "201":
                description: Ok
                headers:
                  Location:
                    schema:
                      type: string
                    description: URI of the created [Entityname]
                content:
                  application/json:
                    schema:
                      $ref: '#/components/schemas/[Entityname]Response'
              '400':
                description: Error in the request
                content:
                  application/json:
                    schema:
                      $ref: "#/components/schemas/ErrorResponse"
          get:
            tags:
              - [entityname]
            summary: Retrieve all [EntitynamePlural]
            operationId: list[Entityname]
            responses:
              "200":
                description: Ok
                content:
                  application/json:
                    schema:
                      type: object
                      required:
                        - results
                      properties:
                        results:
                          type: array
                          items:
                            $ref: '#/components/schemas/[Entityname]Response'
        /[entitynamePlural]/{[entityname]Id}:
          parameters:
            - $ref: "#/components/parameters/[Entityname]Id"
          get:
            tags:
              - [entityname]
            summary: Retrieve [Entityname]
            operationId: get[Entityname]
            responses:
              "200":
                description: Ok
                content:
                  application/json:
                    schema:
                      $ref: '#/components/schemas/[Entityname]Response'
              "404":
                description: No [Entityname] found with this [Entityname]Id
          put:
            tags:
              - [entityname]
            summary: Update [Entityname]
            operationId: update[Entityname]
            requestBody:
              required: true
              content:
                application/json:
                  schema:
                    $ref: '#/components/schemas/[Entityname]Request'
            responses:
              "200":
                description: Ok
                content:
                  application/json:
                    schema:
                      $ref: '#/components/schemas/[Entityname]Response'
              '400':
                description: Error in the request
                content:
                  application/json:
                    schema:
                      $ref: "#/components/schemas/ErrorResponse"
              "404":
                description: No [Entityname] found with this [Entityname]Id
          delete:
            tags:
              - [entityname]
            summary: Delete [Entityname]
            operationId: delete[Entityname]
            responses:
              "200":
                description: Ok
              "400":
                description: Error in the request
                content:
                  application/json:
                    schema:
                      $ref: "#/components/schemas/ErrorResponse"
              "404":
                description: No [Entityname] found with this [Entityname]Id
        [Entityname]:
          type: object
          properties:
            id:
              type: string
              format: uuid
              readOnly: true
              description: unique ID. Ignored in requests, required in responses
            name:
              type: string
          required:
            - name
        [Entityname]Request:
          allOf:
            - $ref: '#/components/schemas/[Entityname]'
        [Entityname]Response:
          allOf:
            - $ref: '#/components/schemas/[Entityname]'
          required:
            - id
          ```
- pagination, sorting and search will be added later
- Integration tests
    - SpringBootTest
    - TestRestTemplate
    - Tests for entities are named [Entityname]IT
    - There is a CrudITBase class that provides common functionality
    - Implementing classes provide template method implementations for what is entity specific
    - Testcontainers are used for Postgres and KeyCloak
    - The possible future hierarchy for integration tests is:
        - TestContainersITBase: Init and provide Testcontainers
            - RestITBase: Provide the TestRestTemplates including possible authentication
                - CrudITBase: Provide common CRUD functionality
                    - [Entityname]IT: Implement entity specific template methods from CrudITBase
- The REST API takes advantage of the OpenAPI generated delegates. This means that it
  just implements the generated [Entityname]ApiDelegate interface
- use JPA for persistence with Spring repositories
- use mapstruct to map between entities and the DTOs generated by OpenAPI
