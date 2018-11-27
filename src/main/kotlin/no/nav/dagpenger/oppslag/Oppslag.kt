package no.nav.dagpenger.oppslag

import com.auth0.jwk.JwkProviderBuilder
import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ktor.application.call
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.application.install
import io.ktor.auth.AuthenticationPipeline
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
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.util.pipeline.Pipeline
import io.prometheus.client.hotspot.DefaultExports
import no.nav.dagpenger.oppslag.ws.arbeidsfordeling.arbeidsfordeling
import no.nav.dagpenger.oppslag.ws.arena.arena
import no.nav.dagpenger.oppslag.ws.joark.joark
import no.nav.dagpenger.oppslag.ws.person.person
import no.nav.dagpenger.oppslag.ws.Clients
import java.util.Date
import java.util.concurrent.TimeUnit

fun main() {
    val env = Environment()
    val app = App(env)

    app.start()

    Runtime.getRuntime().addShutdownHook(Thread {
        app.stop()
    })
}

class App(env: Environment = Environment()) {
    private val nettyServer: NettyApplicationEngine
    private val clients: Clients = Clients(env)

    private val jwkProvider = JwkProviderBuilder(env.jwksUrl)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    init {
        DefaultExports.initialize()

        nettyServer = embeddedServer(Netty, env.httpPort) {
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
                    realm = "Helse Sparkel"
                    verifier(jwkProvider, env.jwtIssuer)
                    validate { credentials ->
                        if (credentials.payload.subject in listOf("srvspinne", "srvsplitt")) {
                            JWTPrincipal(credentials.payload)
                        }
                        null
                    }
                }
            }

            routing {
                authenticate {
                    person(clients.personClient)
                    arbeidsfordeling(clients.arbeidsfordelingClient)
                    arena(clients.arenaClient)
                    joark(clients.joarkClient)
                }

                get("/isAlive") {
                    call.respondText("ALIVE", ContentType.Text.Plain)
                }
                get("/isReady") {
                    call.respondText("READY", ContentType.Text.Plain)
                }
            }
        }
    }

    fun start() {
        nettyServer.start(wait = false)
    }

    fun stop() {
        nettyServer.stop(5, 60, TimeUnit.SECONDS)
    }
}
