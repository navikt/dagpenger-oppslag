FROM navikt/java:11 AS base

FROM base AS dev-env
COPY . /app
RUN /app/gradlew build

FROM base AS release
COPY build/libs/*.jar /app/app.jar
