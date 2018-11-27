package no.nav.dagpenger.oppslag

data class Environment(
    val securityTokenUsername: String = getEnvVar("SRVDAGPENGER_OPPSLAG_USERNAME"),
    val securityTokenPassword: String = getEnvVar("SRVDAGPENGER_OPPSLAG_PASSWORD"),
    val securityTokenServiceEndpointUrl: String = getEnvVar("SECURITYTOKENSERVICE_URL"),
    val personUrl: String = getEnvVar("VIRKSOMHET_PERSON_V3_ENDPOINTURL"),
    val arbeidsfordelingUrl: String = getEnvVar("VIRKSOMHET_ARBEIDSFORDELING_V1_ENDPOINTURL"),
    val arenaOppgaveUrl: String = getEnvVar("VIRKSOMHET_BEHANDLEARBEIDOGAKTIVITETOPPGAVE_V1_ENDPOINTURL"),
    val arenaHentSakerUrl: String = getEnvVar("DAGPENGER_ARENA_HENTSAKER_URL"),
    val inngaaendeJournalUrl: String = getEnvVar("BEHANDLEINNGAAENDEJOURNAL_V1_ENDPOINTURL"),
    val fasitEnvironmentName: String = getEnvVar(
        "FASIT_ENVIRONMENT_NAME",
        ""
    ).filterNot { it in "p" }, //filter out production
    val httpPort: Int = 8080,
    val jwksUrl: String = getEnvVar("JWKS_URL"),
    val jwtIssuer: String = getEnvVar("JWT_ISSUER"),
    val jwtAudience: String = getEnvVar("JWT_AUDIENCE")
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
