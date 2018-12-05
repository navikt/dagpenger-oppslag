package no.nav.dagpenger.oppslag

data class Environment(
    val username: String = getEnvVar("SRVDAGPENGER_OPPSLAG_USERNAME"),
    val password: String = getEnvVar("SRVDAGPENGER_OPPSLAG_PASSWORD"),
    val oicdStsUrl: String = getEnvVar("SECURITYTOKENSERVICE_URL"),
    val dagpengerPersonUrl: String = getEnvVar("VIRKSOMHET_PERSON_V3_ENDPOINTURL"),
    val dagpengerArbeidsfordelingUrl: String = getEnvVar("VIRKSOMHET_ARBEIDSFORDELING_V1_ENDPOINTURL"),
    val dagpengerArenaOppgaveUrl: String = getEnvVar("VIRKSOMHET_BEHANDLEARBEIDOGAKTIVITETOPPGAVE_V1_ENDPOINTURL"),
    val dagpengerArenaHentSakerUrl: String = getEnvVar("DAGPENGER_ARENA_HENTSAKER_URL", "PLACEHOLDER"),
    val dagpengerInngaaendeJournalUrl: String = getEnvVar("BEHANDLEINNGAAENDEJOURNAL_V1_ENDPOINTURL"),
    val fasitEnvironmentName: String = getEnvVar("FASIT_ENVIRONMENT_NAME", "").filterNot { it in "p" }, //filter out productiony
    val httpPort: Int = 8080
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
        System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
