package no.nav.dagpenger.oppslag

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

private val defaultProperties = ConfigurationMap(
    mapOf(
        "application.httpPort" to 8080.toString(),
        "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret/api",
        "allow.insecure.soap.requests" to false.toString()
    )
)
private val localProperties = ConfigurationMap(
    mapOf(
        "application.profile" to Profile.LOCAL.toString(),
        "application.auth.enabled" to false.toString(),
        "srvdagpenger.oppslag.username" to "localhost",
        "srvdagpenger.oppslag.password" to "5432",
        "securitytokenservice.url" to "http://localhost:8079/sts/authorize",
        "aktor.url" to "",
        "virksomhet.person.v3.endpointurl" to "http://localhost:8079/person",
        "virksomhet.arbeidsfordeling.v1.endpointurl" to "http://localhost:8079/arbeidsfordeling",
        "virksomhet.behandlearbeidogaktivitetoppgave.v1.endpointurl" to "http://",
        "journalfoerinngaaende.v1.url" to "http://mockserver:8080/",
        "virksomhet.inntekt.v3.endpointurl" to "http://mockserver:8080/",
        "allow.insecure.soap.requests" to true.toString(),
        "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret/api",
        "jwks.url" to "http://localhost:4352/certs",
        "jwt.issuer" to "http://localhost:4353"
    )
)
private val devProperties = ConfigurationMap(
    mapOf(
        "application.profile" to Profile.DEV.toString(),
        "securitytokenservice.url" to "https://sts-t4.test.local/SecurityTokenServiceProvider/",
        "aktor.url" to "https://app-t4.adeo.no/aktoerregister/api",
        "virksomhet.person.v3.endpointurl" to "https://wasapp-t4.adeo.no/tpsws/ws/Person/v3",
        "virksomhet.arbeidsfordeling.v1.endpointurl" to "https://app-t4.adeo.no/norg2/ws/Arbeidsfordeling/v1",
        "virksomhet.behandlearbeidogaktivitetoppgave.v1.endpointurl" to "https://arena-t4.adeo.no/ail_ws/BehandleArbeidOgAktivitetOppgave_v1",
        "journalfoerinngaaende.v1.url" to "https://dokarkiv-t4.nais.preprod.local/rest/journalfoerinngaaende/v1",
        "virksomhet.inntekt.v3.endpointurl" to "https://app-t4.adeo.no/inntektskomponenten-ws/inntekt/v3/Inntekt",
        "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret/api",
        "jwks.url" to "https://security-token-service-t4.nais.preprod.local/rest/v1/sts/jwks",
        "jwt.issuer" to "https://security-token-service-t4.nais.preprod.local"
    )
)
private val prodProperties = ConfigurationMap(
    mapOf(
        "application.profile" to Profile.PROD.toString(),
        "securitytokenservice.url" to "https://sts.adeo.no/SecurityTokenServiceProvider/",
        "aktor.url" to "https://app.adeo.no/aktoerregister/api",
        "virksomhet.person.v3.endpointurl" to "https://wasapp.adeo.no/tpsws/ws/Person/v3",
        "virksomhet.arbeidsfordeling.v1.endpointurl" to "https://app.adeo.no/norg2/ws/Arbeidsfordeling/v1",
        "virksomhet.behandlearbeidogaktivitetoppgave.v1.endpointurl" to "https://arena.adeo.no/ail_ws/BehandleArbeidOgAktivitetOppgave_v1",
        "journalfoerinngaaende.v1.url" to "https://dokarkiv.nais.adeo.no/rest/journalfoerinngaaende/v1",
        "virksomhet.inntekt.v3.endpointurl" to "https://app.adeo.no/inntektskomponenten-ws/inntekt/v3/Inntekt",
        "enhetsregisteret.url" to "https://data.brreg.no/enhetsregisteret/api",
        "jwks.url" to "https://security-token-service.nais.adeo.no/rest/v1/sts/jwks",
        "jwt.issuer" to "https://security-token-service.nais.adeo.no"
    )
)

private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
    "dev-fss" -> systemProperties() overriding EnvironmentVariables overriding devProperties overriding defaultProperties
    "prod-fss" -> systemProperties() overriding EnvironmentVariables overriding prodProperties overriding defaultProperties
    else -> {
        systemProperties() overriding EnvironmentVariables overriding localProperties overriding defaultProperties
    }
}

internal data class Configuration(
    val soapSTSClient: SoapSTSClient = SoapSTSClient(),
    val auth: Auth = Auth(),
    val personApi: PersonApi = PersonApi(),
    val aktorApi: AktorApi = AktorApi(),
    val arbeidsfordelingApi: ArbeidsfordelingApi = ArbeidsfordelingApi(),
    val arenaOppgaveApi: ArenaOppgaveApi = ArenaOppgaveApi(),
    val arenaHentSakerApi: ArenaHentSakerApi = ArenaHentSakerApi(),
    val inngaaendeJournalApi: InngaaendeJournalApi = InngaaendeJournalApi(),
    val enhetsregisterApi: EnhetsregisterApi = EnhetsregisterApi(),
    val application: Application = Application()

) {
    data class SoapSTSClient(
        val endpoint: String = config()[Key("securitytokenservice.url", stringType)],
        val username: String = config()[Key("srvdagpenger.oppslag.username", stringType)],
        val password: String = config()[Key("srvdagpenger.oppslag.password", stringType)],
        val allowInsecureSoapRequests: Boolean = config()[Key("allow.insecure.soap.requests", booleanType)]
    )

    data class Auth(
        val jwksUrl: String = config()[Key("jwks.url", stringType)],
        val issuer: String = config()[Key("jwt.issuer", stringType)],
        val username: String = config()[Key("srvdagpenger.oppslag.username", stringType)],
        val password: String = config()[Key("srvdagpenger.oppslag.password", stringType)]
    )

    data class PersonApi(
        val endpoint: String = config()[Key("virksomhet.person.v3.endpointurl", stringType)]
    )

    data class AktorApi(
        val endpoint: String = config()[Key("aktor.url", stringType)]
    )

    data class ArbeidsfordelingApi(
        val endpoint: String = config()[
            Key(
                "virksomhet.arbeidsfordeling.v1.endpointurl",
                stringType
            )
        ]
    )

    data class ArenaOppgaveApi(
        val endpoint: String = config()[
            Key(
                "virksomhet.behandlearbeidogaktivitetoppgave.v1.endpointurl",
                stringType
            )
        ]
    )

    data class ArenaHentSakerApi(
        val endpoint: String = config()[Key("virksomhet.person.v3.endpointurl", stringType)]
    )

    data class InngaaendeJournalApi(
        val endpoint: String = config()[Key("journalfoerinngaaende.v1.url", stringType)]
    )

    data class EnhetsregisterApi(
        val endpoint: String = config()[Key("enhetsregisteret.url", stringType)]
    )

    data class Application(
        val profile: Profile = config()[Key("application.profile", stringType)].let { Profile.valueOf(it) },
        val httpPort: Int = config()[Key("application.httpPort", intType)]
    )
}

enum class Profile {
    LOCAL, DEV, PROD
}
