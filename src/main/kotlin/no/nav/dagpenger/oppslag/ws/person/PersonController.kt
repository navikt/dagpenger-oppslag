package no.nav.dagpenger.oppslag.ws.person

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.person(personClient: PersonClientSoap) {
    post("api/person/geografisk-tilknytning") {
        val fødselsnummer = call.receive<String>()
        val geografiskTilknytningResponse = personClient.getGeografiskTilknytning(fødselsnummer)

        call.respond(geografiskTilknytningResponse)
    }
}