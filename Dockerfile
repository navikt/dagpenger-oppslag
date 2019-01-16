FROM navikt/java:11-appdynamics

COPY build/libs/*.jar app.jar
