# Dagpenger-oppslag

Bindeledd mellom soap-tjenester og resttjenester og dagpenger-mikrotjenestene

# Komme i gang

Gradle brukes som byggverktøy og er bundlet inn.

`./gradlew build`

# Lokal utvikling

Når du utvikler kan du bruke (docker-compose)[] som setter opp mocks av eksterne avhengigheter.

For å bygge og kjøre en lokal instans av holder det å kjøre:

`docker-compose up`

Det både bygger og kjører opp en instans av `dagpenger-oppslag` på port 8080. Ønsker
du å kjøre `dagpenger-oppslag` i IntelliJ kan du kjøre opp kun mockserveren med:

`docker-compose up mockserver-local`

Da vil den kjøres opp og eksponeres på port 8079 (og default dev-config i `Configuration.kt`
vil fungere).

## mockserver

Ulike NAV-tjenester er mocket i `mocked-services` katalogen, som er
en liten Express.js app. Docker-compose setter den opp slik at endringer lastes
automatisk (bortsett fra de som gjøres i XML-filene).

## Litt om hvordan docker bygger.

Dockerfila bruker en multi-stage build og kan brukes både av CI (Jenkins) og lokalt.
Jenkins er satt opp til å bygge på utsiden av Docker som vanlig, men lokalt så bygges 
applikasjonen i Docker.

Docker-compose bygger kun opp til `dev-env` i Dockerfila, og vil dermed ikke prøve å 
kopiere inn Jar-fila lokalt.

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

- André Roaldseth, andre.roaldseth@nav.no
- Eller en annen måte for omverden å kontakte teamet på

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #dagpenger.
