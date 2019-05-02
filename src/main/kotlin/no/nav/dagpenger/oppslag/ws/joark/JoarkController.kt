package no.nav.dagpenger.oppslag.ws.joark

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.Success

fun Route.joark(joarkClient: JoarkClient) {
    post("api/joark/ferdigstill") {
        val request = call.receive<JoarkFerdigstillRequest>()
        val result = joarkClient.ferdigstillJournalføring(request.journalpostId)

        when (result) {
            is Success<*> -> call.respond(HttpStatusCode.OK)
            is Failure -> call.respond(HttpStatusCode.InternalServerError, "Error")
        }
    }
}

data class JoarkFerdigstillRequest(val journalpostId: String)