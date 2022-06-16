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

## Enabling Spring Security
Spring security with Firebase authorization is currently included in the User Management API. In order to enable 
this, you will need to:
1. Remove the comment on this line in `build.gradle`
    ```
    implementation 'com.google.cloud:spring-cloud-gcp-starter-secretmanager:2.0.10'
    ```
2. Remove the comments and include necessary imports in the following files
   1. `service/src/main/java/io/nuvalence/user/management/api/service/auth/WebSecurityConfig.java`
   2. `service/src/main/java/io/nuvalence/user/management/api/service/config/RunAfterStartUp.java`
   3. `service/src/test/java/io/nuvalence/user/management/api/service/auth/JwtUtilityTest.java`

Once this is reenabled, in order to launch the application context, google credential's need to be applied to access
secret manager's resources (API Key, and Firebase Config). Simply run the following gcloud commands:
```shell
gcloud init
```
Use this to set the default account to the project you need to access.

```shell
gcloud auth application-default login
```
This will prompt you to authorize through google with your email.

* Lastly you will need to create an environment variable CLOUDSDK_CONFIG that points to google's default credentials.
  <br>

Mac
```shell
$HOME/.config/gcloud
```

Linux location:
```shell
~/.config/gcloud
```
Windows location:
```shell 
C:\Users\%username%\AppData\Roaming\gcloud
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