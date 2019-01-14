package no.nav.dagpenger.oppslag

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.prometheus.client.hotspot.DefaultExports
import no.nav.dagpenger.oppslag.ws.Clients
import no.nav.dagpenger.oppslag.ws.inntekt.InntektClient
import no.nav.dagpenger.oppslag.ws.inntekt.inntekt
import no.nav.dagpenger.oppslag.ws.joark.JoarkClient
import no.nav.dagpenger.oppslag.ws.joark.joark
import no.nav.dagpenger.oppslag.ws.person.PersonClientSoap
import no.nav.dagpenger.oppslag.ws.person.person
import no.nav.dagpenger.oppslag.ws.sts.STS_SAML_POLICY_NO_TRANSPORT_BINDING
import no.nav.dagpenger.oppslag.ws.sts.configureFor
import no.nav.dagpenger.oppslag.ws.sts.stsClient
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1
import no.nav.tjeneste.virksomhet.inntekt.v3.binding.InntektV3
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import java.net.URL
import java.util.Date
import java.util.concurrent.TimeUnit

private val authorizedUsers = listOf("srvdp-jrnf-ruting", "srvdp-jrnf-ferdig", "srvdp-inntekt-api")

fun main() {
    val env = Environment()

    DefaultExports.initialize()

    val app = embeddedServer(Netty, 8080) {
        val jwkProvider = JwkProviderBuilder(URL(env.jwksUrl))
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

        oppslag(env, jwkProvider)
    }

    app.start(wait = false)

    Runtime.getRuntime().addShutdownHook(Thread {
        app.stop(5, 60, TimeUnit.SECONDS)
    })
}

fun Application.oppslag(env: Environment, jwkProvider: JwkProvider) {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        moshi {
            add(KotlinJsonAdapterFactory())
            add(Date::class.java, Rfc3339DateJsonAdapter())
        }
    }

    install(Authentication) {
        jwt {
            realm = "Dagpenger Oppslag"
            verifier(jwkProvider, env.jwtIssuer)
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

    val stsClient by lazy {
        stsClient(
            env.securityTokenServiceEndpointUrl,
            env.securityTokenUsername to env.securityTokenPassword
        )
    }

    routing {
        authenticate {
            joark {
                val port = Clients.createServicePort(
                    endpoint = env.inngaaendeJournalUrl,
                    service = BehandleInngaaendeJournalV1::class.java
                )

                if (env.allowInsecureSoapRequests) {
                    stsClient.configureFor(port, STS_SAML_POLICY_NO_TRANSPORT_BINDING)
                } else {
                    stsClient.configureFor(port)
                }

                JoarkClient(port)
            }
            person {
                val port = Clients.createServicePort(
                    endpoint = env.personUrl,
                    service = PersonV3::class.java
                )

                if (env.allowInsecureSoapRequests) {
                    stsClient.configureFor(port, STS_SAML_POLICY_NO_TRANSPORT_BINDING)
                } else {
                    stsClient.configureFor(port)
                }

                PersonClientSoap(port)
            }
            inntekt {
                val port = Clients.createServicePort(env.inntektEndpointUrl, InntektV3::class.java)
                if (env.allowInsecureSoapRequests) {
                    stsClient.configureFor(port, STS_SAML_POLICY_NO_TRANSPORT_BINDING)
                } else {
                    stsClient.configureFor(port)
                }
                InntektClient(port)
            }
        }

        get("/isAlive") {
            call.respondText("ALIVE", ContentType.Text.Plain)
        }
        get("/isReady") {
            call.respondText("READY", ContentType.Text.Plain)
        }
    }
}
