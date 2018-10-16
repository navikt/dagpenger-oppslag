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
import no.nav.dagpenger.oppslag.person.PersonClientSoap
import no.nav.dagpenger.oppslag.person.person
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3

private val LOGGER = KotlinLogging.logger {}

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8090, module = Application::main).start(wait = true)
}

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)

    val wsClient = WsClient<PersonV3>("", "", "")
    val person = wsClient.createPortForSystemUser("", PersonV3::class.java)

    val personClient = PersonClientSoap(person)
    val arenaClient = ArenaClientDummy()

    routing {
        person(personClient)
        arena(arenaClient)
    }
}