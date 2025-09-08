# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

This is a Spring Boot banking application called "ansimbank" located in the `ansimbank/` subdirectory. The project uses:

- **Framework**: Spring Boot 3.5.5 with Java 17
- **Build Tool**: Gradle 8.14.3
- **Database**: MySQL with Spring Data JPA
- **Security**: Spring Security
- **Additional**: Lombok for boilerplate reduction, validation support

## Development Commands

All commands should be run from the `ansimbank/` directory:

### Build and Run
- `./gradlew build` - Build the project
- `./gradlew bootRun` - Run the Spring Boot application
- `./gradlew clean` - Clean build artifacts
- `./gradlew bootJar` - Create executable JAR

### Testing
- `./gradlew test` - Run all tests
- `./gradlew bootTestRun` - Run application with test runtime classpath

### Other Useful Commands
- `./gradlew tasks` - List all available Gradle tasks
- `./gradlew bootBuildImage` - Build OCI container image

## Architecture Notes

- The main application class is `AnsimbankApplication.java` in package `com.grandma.ansimbank`
- Currently a minimal Spring Boot setup with only the main application class
- Configured with Spring Data JPA, Security, Web, and Validation starters
- Database configuration should be added to `application.properties` for MySQL connectivity