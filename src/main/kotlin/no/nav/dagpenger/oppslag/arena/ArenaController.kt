package no.nav.dagpenger.oppslag.arena

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.post
import no.nav.arena.services.lib.sakvedtak.SaksInfo
import no.nav.arena.services.sakvedtakservice.FaultFeilIInputMsg

fun Routing.arena(arenaClient: ArenaClientSoap) {
    post("arena/opprettsak") {
        val (behandlendeEnhetId, fødselsnummer) = call.receive<BestillArenaSakRequest>()

        val arenaSakId = arenaClient.bestillOppgave(behandlendeEnhetId, fødselsnummer)

        call.respondText(arenaSakId)
    }

    post("arena/finnsak") {
        val fødselsnummer = call.receive<String>()

        try {
            val saker = arenaClient.getDagpengerSaker(fødselsnummer, "PERSON", false)

            val newestActiveSak = findNewestActiveSak(saker)

            if (newestActiveSak == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(newestActiveSak.saksId)
            }
        } catch (inputException : FaultFeilIInputMsg) {
            call.respond(HttpStatusCode.BadRequest, inputException.faultInfo)
        }
    }
}

fun findNewestActiveSak(saker : List<SaksInfo>) : SaksInfo? {
    return saker.filter { it.sakstatus == "AKTIV" }.maxBy { it.sakOpprettet.toGregorianCalendar() }
}

data class BestillArenaSakRequest(
    val behandlendeEnhetId: String,
    val fødselsnummer: String
)