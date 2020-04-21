package no.nav.dagpenger.oppslag.ws.brreg.enhetsregister

import com.ryanharter.ktor.moshi.moshi
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.dagpenger.oppslag.moshiInstance

fun main() {
    embeddedServer(Netty, port = 8081) {
        install(DefaultHeaders)
        install(ContentNegotiation) {
            moshi(moshiInstance)
        }
        routing {
            enhetRegister(EnhetsRegisteretHttpClient("https://data.brreg.no/enhetsregisteret/api"))
        }
    }.start(true)
}
