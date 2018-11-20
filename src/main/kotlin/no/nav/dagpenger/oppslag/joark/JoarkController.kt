package no.nav.dagpenger.oppslag.joark

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.joark(joarkClient: JoarkClientSoap) {
    post("api/joark/ferdigstill") {
        val request = call.receive<JoarkFerdigstillRequest>()

        val response = joarkClient.ferdigstillJournalføring(request.journalpostId)

        call.respond(response)
    }
}

data class JoarkFerdigstillRequest(val journalpostId: String)