# syntax=docker/dockerfile:experimental
FROM navikt/java:11 AS base

FROM base AS dev-env

# Speed up by copying in gradle wrapper separately (and install gradle)
COPY gradlew /app
COPY gradle /app/gradle
RUN /app/gradlew help -i

COPY . /app
# Enable when docker-compose supports BuildKit
#RUN --mount=type=cache,target=/root/.gradle/caches/ /app/gradlew build -i
RUN /app/gradlew build -i && \
  mv /app/build/libs/*.jar /app/app.jar

FROM base AS release
COPY build/libs/*.jar /app/app.jar
