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

    post("api/arena/createoppgave") {
        val createArenaOppgaveRequest = call.receive<CreateArenaOppgaveRequest>()

        val sakId = arenaClient.createOppgave(createArenaOppgaveRequest)

        call.respond(CreateArenaOppgaveResponse(sakId))
    }

    post("api/arena/getsaker") {
        val findArenaSakRequest = call.receive<GetArenaSakerRequest>()

        try {
            val saker = arenaClient.getDagpengerSaker(findArenaSakRequest)
            call.respond(saker)
        } catch (inputException: FaultFeilIInputMsg) {
            call.respond(HttpStatusCode.BadRequest, inputException.faultInfo)
        }
    }
}

data class CreateArenaOppgaveRequest(
    val behandlendeEnhetId: String,
    val fødselsnummer: String,
    val sakId: String?,
    val oppgaveType: String,
    val tema: String,
    val prioritet: String,
    val tvingNySak: Boolean
)

data class CreateArenaOppgaveResponse(
    val sakId: String
)

data class GetArenaSakerRequest(
    val fødselsnummer: String,
    val brukerType: String,
    val tema: String,
    val includeInactive: Boolean
)

data class GetArenaSakerResponse(
    val saker: List<SaksInfo>
)
