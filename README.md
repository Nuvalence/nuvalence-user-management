# Nuvalence User Management API build and run:

## Build/Run Locally:
```./gradlew clean build composeUp```

You can view the state of the database afterwards by going to the local instance of [pgAdmin](http://localhost:5050) with the following credentials:

- Email Address / Username: **admin@admin.com**
- Password: **root**

Then, register a server with the following details:

- Name: **userapipostgres** (can be anything though)
- Host name/address: **userapipostgres**
- Port: **5439**
- Username: **root**
- Password: **root**

NOTE: These details are also in the [docker-compose.yml](./docker-compose.yml) file.

[view docs](http://localhost:8080/swagger-ui.html)

The app can be brought down via:

```./gradlew composeDown```

## Debug:
```gradle bootRun -Dagentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000```

### Documentation
 - [tools and frameworks](./docs/tools.md)

## Cerbos
The Nuvalence User Management has a dependency to a Cerbos instance with the Cerbos Admin API configured and running for updates to roles and permissions as well as validating permissions. You can view the Cerbos docs [here](https://docs.cerbos.dev/cerbos/latest/index.html).

Once valid Cerbos instance is up and running, edit the `cerbos` section in the corresponding [application.yml](service/src/main/resources/application.yml) file.
```yaml
cerbos:
  baseUrl: "https://cerbos-url.com"
  username: "cerbosUser"
  password: "cerbosPassword"
```

## Contributors
The Nuvalence User Management API was originally a private project with contributions from:
- [@Mark-The-Dev](https://github.com/Mark-The-Dev)
- [@jazelke](https://github.com/jazelke)
- [@gcastro12](https://github.com/gcastro12)
- [@gmisail](https://github.com/gmisail)
- [@apengu](https://github.com/apengu)
- [@HarisShahidNuvalence](https://github.com/HarisShahidNuvalence)
- [@dtsong](https://github.com/dtsong)