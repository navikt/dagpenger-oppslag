package no.nav.dagpenger.oppslag

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.JsonDataException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import mu.KotlinLogging
import no.nav.dagpenger.oidc.StsOidcClient
import no.nav.dagpenger.oppslag.ws.SoapPort
import no.nav.dagpenger.oppslag.ws.aktor.AktorRegisterHttpClient
import no.nav.dagpenger.oppslag.ws.aktor.aktorRegister
import no.nav.dagpenger.oppslag.ws.joark.JoarkClient
import no.nav.dagpenger.oppslag.ws.joark.joark
import no.nav.dagpenger.oppslag.ws.person.PersonClient
import no.nav.dagpenger.oppslag.ws.person.person
import no.nav.dagpenger.oppslag.ws.sts.STS_SAML_POLICY_NO_TRANSPORT_BINDING
import no.nav.dagpenger.oppslag.ws.sts.configureFor
import no.nav.dagpenger.oppslag.ws.sts.stsClient
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

private val authorizedUsers = listOf("srvdp-jrnf-ruting", "srvdp-jrnf-ferdig", "srvdp-inntekt-api")
private val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry

private val LOGGER = KotlinLogging.logger {}

fun main() {
    val env = Environment()

    DefaultExports.initialize()

    val jwkProvider = JwkProviderBuilder(URL(env.jwksUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val stsClient by lazy {
        stsClient(
            env.securityTokenServiceEndpointUrl,
            env.securityTokenUsername to env.securityTokenPassword
        )
    }

    val oidcClient by lazy {
        StsOidcClient(env.jwtIssuer, env.securityTokenUsername, env.securityTokenPassword)
    }

    val joarkClient = JoarkClient(env.inngaaendeJournalUrl)

    val aktorRegisterClient = AktorRegisterHttpClient(env.aktorOppslagUrl, oidcClient)

    val personPort = SoapPort.PersonV3(env.personUrl)
    val personClient = PersonClient(personPort)

    if (env.allowInsecureSoapRequests) {
        stsClient.configureFor(personPort, STS_SAML_POLICY_NO_TRANSPORT_BINDING)
    } else {
        stsClient.configureFor(personPort)
    }

    val app = embeddedServer(Netty, 8080) {
        oppslag(env, jwkProvider, joarkClient, personClient, aktorRegisterClient)
    }

    app.start(wait = false)

    Runtime.getRuntime().addShutdownHook(Thread {
        app.stop(5, 60, TimeUnit.SECONDS)
    })
}

fun Application.oppslag(
    env: Environment,
    jwkProvider: JwkProvider,
    joarkClient: JoarkClient,
    personClient: PersonClient,
    aktorRegisterClient: AktorRegisterHttpClient
) {

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        moshi(moshiInstance)
    }
    install(StatusPages) {
        exception<JsonDataException> { cause ->
            LOGGER.warn("Request does not match expected json", cause)
            val error = Problem(
                type = URI("urn:dp:error:oppslag:parameter"),
                title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                status = 400
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
    }

    install(Authentication) {
        jwt {
            realm = "Dagpenger Oppslag"
            verifier(jwkProvider, env.jwtIssuer)
            authHeader { call ->
                call.request.cookies["ID_token"]?.let {
                    HttpAuthHeader.Single("Bearer", it)
                } ?: call.request.parseAuthorizationHeader()
            }
            validate { credentials ->
                if (credentials.payload.subject in authorizedUsers) {
                    log.info("authorization ok")
                    return@validate JWTPrincipal(credentials.payload)
                } else {
                    log.info("authorization failed")
                    return@validate null
                }
            }
        }
    }

    routing {
        authenticate {
            joark(joarkClient)
            person(personClient)
            aktorRegister(aktorRegisterClient)
        }

        get("/isAlive") {
            call.respondText("ALIVE", ContentType.Text.Plain)
        }
        get("/isReady") {
            call.respondText("READY", ContentType.Text.Plain)
        }
        get("/metrics") {
            val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: setOf()
            call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
            }
        }
    }
}
