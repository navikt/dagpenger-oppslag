package no.nav.dagpenger.oppslag

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging
import no.nav.dagpenger.oppslag.arena.ArenaClientDummy
import no.nav.dagpenger.oppslag.arena.arena
import no.nav.dagpenger.oppslag.person.PersonClientDummy
import no.nav.dagpenger.oppslag.person.person

private val LOGGER = KotlinLogging.logger {}

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8090, module = Application::main).start(wait = true)
}

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)

    val personClient = PersonClientDummy()
    val arenaClient = ArenaClientDummy()

    routing {
        person(personClient)
        arena(arenaClient)
    }
}