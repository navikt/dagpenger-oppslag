package no.nav.dagpenger.oppslag

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

data class Environment(val map: Map<String, String> = System.getenv()) {
    val securityTokenUsername: String by lazyEnvVar("SRVDAGPENGER_OPPSLAG_USERNAME")
    val securityTokenPassword: String by lazyEnvVar("SRVDAGPENGER_OPPSLAG_PASSWORD")
    val securityTokenServiceEndpointUrl: String by lazyEnvVar("SECURITYTOKENSERVICE_URL")
    val personUrl: String by lazyEnvVar("VIRKSOMHET_PERSON_V3_ENDPOINTURL")
    val arbeidsfordelingUrl: String by lazyEnvVar("VIRKSOMHET_ARBEIDSFORDELING_V1_ENDPOINTURL")
    val arenaOppgaveUrl: String by lazyEnvVar("VIRKSOMHET_BEHANDLEARBEIDOGAKTIVITETOPPGAVE_V1_ENDPOINTURL")
    val arenaHentSakerUrl: String by lazyEnvVar("DAGPENGER_ARENA_HENTSAKER_URL")
    val inngaaendeJournalUrl: String by lazyEnvVar("BEHANDLEINNGAAENDEJOURNAL_V1_ENDPOINTURL")
    val fasitEnvironmentName: String by lazyEnvVar(
        "FASIT_ENVIRONMENT_NAME"
    ) // .filterNot { it in "p" } //filter out production

    val httpPort: Int = 8080
    val jwksUrl: String by lazyEnvVar("JWKS_URL")
    val jwtIssuer: String by lazyEnvVar("JWT_ISSUER")
    val jwtAudience: String by lazyEnvVar("JWT_AUDIENCE")

    val allowInsecureSoapRequests: Boolean by lazyEnvVar(
        "ALLOW_INSECURE_SOAP_REQUESTS",
        "false"
    ) { value -> "true" == value }

    private fun lazyEnvVar(key: String): ReadOnlyProperty<Environment, String> {
        return lazyEnvVar(key, null) { value -> value }
    }

    private fun <R> lazyEnvVar(
        key: String,
        defaultValue: String? = null,
        mapper: ((String) -> R)
    ): ReadOnlyProperty<Environment, R> {
        return object : ReadOnlyProperty<Environment, R> {
            override operator fun getValue(thisRef: Environment, property: KProperty<*>) =
                mapper(envVar(key, defaultValue))
        }
    }

    private fun envVar(key: String, defaultValue: String? = null): String {
        return map[key] ?: defaultValue ?: throw RuntimeException("Missing required variable \"$key\"")
    }
}
