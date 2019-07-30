# syntax=docker/dockerfile:experimental
FROM navikt/java:11 AS base

FROM base AS dev-env

# Speed up by copying in gradle wrapper separately (and install gradle)
COPY gradlew gradle.properties /app
COPY gradle /app/gradle
RUN /app/gradlew help -i

COPY . /app
RUN --mount=type=cache,target=/root/.gradle/caches/ /app/gradlew build -i

FROM base AS release
COPY build/libs/*.jar /app/app.jar
