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
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import mu.KotlinLogging
import no.nav.dagpenger.oidc.StsOidcClient
import no.nav.dagpenger.oppslag.ws.SoapPort
import no.nav.dagpenger.oppslag.ws.aktor.AktorRegisterHttpClient
import no.nav.dagpenger.oppslag.ws.aktor.aktorRegister
import no.nav.dagpenger.oppslag.ws.brreg.enhetsregister.EnhetsRegisteretHttpClient
import no.nav.dagpenger.oppslag.ws.brreg.enhetsregister.enhetRegister
import no.nav.dagpenger.oppslag.ws.joark.JoarkClient
import no.nav.dagpenger.oppslag.ws.joark.joark
import no.nav.dagpenger.oppslag.ws.person.PersonClient
import no.nav.dagpenger.oppslag.ws.person.person
import no.nav.dagpenger.oppslag.ws.sts.STS_SAML_POLICY_NO_TRANSPORT_BINDING
import no.nav.dagpenger.oppslag.ws.sts.configureFor
import no.nav.dagpenger.oppslag.ws.sts.stsClient
import org.slf4j.event.Level
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit

private val authorizedUsers = listOf("srvdp-jrnf-ruting", "srvdp-jrnf-ferdig", "srvdp-inntekt-api")
private val collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry

private val LOGGER = KotlinLogging.logger {}

fun main() {
    val config = Configuration()

    DefaultExports.initialize()

    val jwkProvider = JwkProviderBuilder(URL(config.auth.jwksUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    val stsClient by lazy {
        stsClient(
            stsUrl = config.soapSTSClient.endpoint,
            credentials = config.soapSTSClient.username to config.soapSTSClient.password
        )
    }

    val oidcClient by lazy {
        StsOidcClient(
            stsBaseUrl = config.auth.issuer,
            username = config.auth.username,
            password = config.auth.password
        )
    }

    val joarkClient = JoarkClient(inngåendeJournal = config.inngaaendeJournalApi.endpoint)
    val aktorRegisterClient = AktorRegisterHttpClient(baseUrl = config.aktorApi.endpoint, oidcClient = oidcClient)
    val enhetsRegisterClient = EnhetsRegisteretHttpClient(enhetsRegisteretUrl = config.enhetsregisterApi.endpoint)

    val personPort = SoapPort.PersonV3(serviceUrl = config.personApi.endpoint)
    val personClient = PersonClient(personPort)

    if (config.soapSTSClient.allowInsecureSoapRequests) {
        stsClient.configureFor(personPort, STS_SAML_POLICY_NO_TRANSPORT_BINDING)
    } else {
        stsClient.configureFor(personPort)
    }

    val app = embeddedServer(Netty, config.application.httpPort) {
        oppslag(
            jwkProvider = jwkProvider,
            jwtIssuer = config.auth.issuer,
            joarkClient = joarkClient,
            personClient = personClient,
            aktorRegisterClient = aktorRegisterClient,
            enhetRegisterClient = enhetsRegisterClient
        )
    }

    app.start(wait = false)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            app.stop(5, 60)
        }
    )
}

fun Application.oppslag(
    jwkProvider: JwkProvider,
    jwtIssuer: String,
    joarkClient: JoarkClient,
    personClient: PersonClient,
    aktorRegisterClient: AktorRegisterHttpClient,
    enhetRegisterClient: EnhetsRegisteretHttpClient
) {

    install(DefaultHeaders)
    install(CallLogging) {
        level = Level.INFO

        filter { call ->
            !call.request.path().startsWith("/isAlive") &&
                !call.request.path().startsWith("/isReady") &&
                !call.request.path().startsWith("/metrics")
        }
    }
    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, CollectorRegistry.defaultRegistry, Clock.SYSTEM)
    }
    install(ContentNegotiation) {
        moshi(moshiInstance)
    }
    install(StatusPages) {
        exception<JsonDataException> { cause ->
            LOGGER.warn(cause) { "Request does not match expected json" }
            val error = Problem(
                type = URI("urn:dp:error:oppslag:parameter"),
                title = "Parameteret er ikke gyldig, mangler obligatorisk felt: '${cause.message}'",
                status = 400
            )
            call.respond(HttpStatusCode.BadRequest, error)
        }
        exception<Throwable> { cause ->
            LOGGER.error(cause) { "Request failed!" }
            val error = Problem(
                type = URI("urn:dp:error:oppslag"),
                title = "Uhåndtert feil!"
            )
            call.respond(HttpStatusCode.InternalServerError, error)
        }
    }

    install(Authentication) {
        jwt {
            realm = "Dagpenger Oppslag"

            verifier(jwkProvider, jwtIssuer)

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
        enhetRegister(enhetRegisterClient)
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
