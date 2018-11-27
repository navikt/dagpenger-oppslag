package no.nav.dagpenger.oppslag.ws.arena

import com.squareup.moshi.JsonDataException
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import no.nav.arena.services.sakvedtakservice.FaultFeilIInputMsg
import java.util.Date

fun Route.arena(arenaClient: ArenaClientSoap) {
    post("api/arena/createoppgave") {
        try {
            val createArenaOppgaveRequest = call.receive<CreateArenaOppgaveRequest>()

            val sakId = arenaClient.createOppgave(createArenaOppgaveRequest)

            call.respond(CreateArenaOppgaveResponse(sakId))
        } catch (jsonException: JsonDataException) {
            call.respond(HttpStatusCode.BadRequest, jsonException.message ?: "")
        }
    }

    post("api/arena/getsaker") {
        try {
            val findArenaSakRequest = call.receive<GetArenaSakerRequest>()

            val saker = arenaClient.getDagpengerSaker(findArenaSakRequest)
                .map { sak -> ArenaSak(sak.saksId, sak.sakstatus, sak.sakOpprettet.toGregorianCalendar().time) }

            call.respond(GetArenaSakerResponse(saker))
        } catch (inputException: FaultFeilIInputMsg) {
            call.respond(HttpStatusCode.BadRequest, inputException.faultInfo)
        } catch (jsonException: JsonDataException) {
            call.respond(HttpStatusCode.BadRequest, jsonException.message ?: "")
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

data class ArenaSak(val sakId: String, val sakstatus: String, val opprettet: Date)

data class GetArenaSakerResponse(
    val saker: List<ArenaSak>
)
