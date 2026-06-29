# syntax=docker/dockerfile:1
#
# Self-contained dev image: build the provider JAR, then bake it into Keycloak. No host JDK needed;
# `docker compose up` produces a runnable Keycloak with the Caputchin providers + theme.

# Stage 1: build the provider JAR.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# Stage 2: Keycloak with the provider baked in.
FROM quay.io/keycloak/keycloak:26.0.8
COPY --from=build /build/target/caputchin-keycloak.jar /opt/keycloak/providers/caputchin-keycloak.jar
RUN /opt/keycloak/bin/kc.sh build
