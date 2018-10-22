package no.nav.dagpenger.oppslag

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging
import no.nav.dagpenger.oppslag.arbeidsfordeling.ArbeidsfordelingClientSoap
import no.nav.dagpenger.oppslag.arbeidsfordeling.arbeidsfordeling
import no.nav.dagpenger.oppslag.arena.ArenaClientDummy
import no.nav.dagpenger.oppslag.arena.arena
import no.nav.dagpenger.oppslag.person.PersonClientSoap
import no.nav.dagpenger.oppslag.person.person
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3

private val LOGGER = KotlinLogging.logger {}

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8090, module = Application::main).start(wait = true)
}

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    val wsClientPerson = WsClient<PersonV3>(
            "https://localhost/castlemock/mock/rest/project/o9jGYV/application/7CvpvM/authorize", "user", "pwd")
    val person = wsClientPerson.createPortForSystemUser(
            "https://localhost/castlemock/mock/soap/project/rRm85C/Person_v3Port", PersonV3::class.java)
    val personClient = PersonClientSoap(person)

    val wsClientArbeidsFordeling = WsClient<ArbeidsfordelingV1>(
            "https://localhost/castlemock/mock/rest/project/o9jGYV/application/7CvpvM/authorize", "user", "pwd")
    val arbeidsfordeling = wsClientArbeidsFordeling.createPortForSystemUser(
            "https://localhost/castlemock/mock/soap/project/rRm85C/Arbeidsfordeling_v1Port", ArbeidsfordelingV1::class.java)
    val arbeidsfordelingClient = ArbeidsfordelingClientSoap(arbeidsfordeling)

    val arenaClient = ArenaClientDummy()

    routing {
        person(personClient)
        arena(arenaClient)
        arbeidsfordeling(arbeidsfordelingClient)
    }
}