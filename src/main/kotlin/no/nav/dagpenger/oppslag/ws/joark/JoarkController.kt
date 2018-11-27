package no.nav.dagpenger.oppslag.ws.joark

import io.ktor.application.call
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.joark(joarkClient: JoarkClientSoap) {
    post("api/joark/ferdigstill") {
        val request = call.receive<JoarkFerdigstillRequest>()
        val response = joarkClient.ferdigstillJournalføring(request.journalpostId)
        val foo = call.authentication.principal<JWTPrincipal>()


        call.respond(response)
    }
}

data class JoarkFerdigstillRequest(val journalpostId: String)