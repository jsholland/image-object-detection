# image-object-detection
A Spring Boot service that upload images and optionally detects objects using Google's Vision API.
The following stack is used
- Java 11
- Spring Boot/Data/Rest
- Gradle
- Postgres
- Liquibase

## Running locally
- Since this service uses the Google Vision API, in order to run this service locally you have to have
a Google Cloud account, add a Google Cloud application with a service account that has the API enabled.
  See: https://cloud.google.com/vision/docs/before-you-begin
- Running locally also requires a locally running postgres instance.  I installed a Docker image for postgres
downloaded from dockerhub.  If you intend to run this locally you'll need to match the postgres settings in the
  spring.datasource.* application properties - or change them to match your locally running postgres username
  and password.  The liquibase migration script should setup the required schema on application boot.
- Cross origin access is allowed for http://localhost:4200, which is the default port for locally running Angular
apps.  If you intend to allow access on another port/domain you'll need to configure the ImageResource
  class to allow access.
