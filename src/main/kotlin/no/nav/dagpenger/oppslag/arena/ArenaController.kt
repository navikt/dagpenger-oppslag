package no.nav.dagpenger.oppslag.arena

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post

fun Routing.arena(arenaClient: ArenaClientSoap) {
    post("arena/sak") {
        val (behandlendeEnhetId, fødselsnummer ) = call.receive<BestillArenaSakRequest>()

        val arenaSakId = arenaClient.bestillOppgave(behandlendeEnhetId, fødselsnummer)

        call.respondText(arenaSakId)
    }

    post("arena/hentsak") {
        val (behandlendeEnhetId, fødselsnummer ) = call.receive<BestillArenaSakRequest>()

        val arenaSakId = arenaClient.hentDagpengerSaker(fødselsnummer, "PERSON")

        call.respondText("awe")
    }
}

data class BestillArenaSakRequest(
    val behandlendeEnhetId: String,
    val fødselsnummer: String
)