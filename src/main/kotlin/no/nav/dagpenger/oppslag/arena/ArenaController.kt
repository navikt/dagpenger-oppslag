package no.nav.dagpenger.oppslag.arena

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import no.nav.arena.services.lib.sakvedtak.SaksInfo
import no.nav.arena.services.sakvedtakservice.FaultFeilIInputMsg

fun Routing.arena(arenaClient: ArenaClientSoap) {
    post("api/arena/createsak") {
        val (behandlendeEnhetId, fødselsnummer) = call.receive<CreateArenaSakRequest>()

        val sakId = arenaClient.bestillOppgave(behandlendeEnhetId, fødselsnummer)

        call.respond(ArenaSakResponse(sakId))
    }

    post("api/arena/findsak") {
        val (fødselsnummer) = call.receive<FindArenaSakRequest>()

        try {
            val saker = arenaClient.getDagpengerSaker(fødselsnummer, "PERSON")

            val newestActiveSak = findNewestActiveSak(saker)

            if (newestActiveSak == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(ArenaSakResponse(newestActiveSak.saksId))
            }
        } catch (inputException: FaultFeilIInputMsg) {
            call.respond(HttpStatusCode.BadRequest, inputException.faultInfo)
        }
    }
}

fun findNewestActiveSak(saker: List<SaksInfo>): SaksInfo? {
    return saker.filter { it.sakstatus == "AKTIV" }.maxBy { it.sakOpprettet.toGregorianCalendar() }
}

data class CreateArenaSakRequest(
    val behandlendeEnhetId: String,
    val fødselsnummer: String
)

data class FindArenaSakRequest(
    val fødselsnummer: String
)

data class ArenaSakResponse(
    val arenaSakId: String
)
