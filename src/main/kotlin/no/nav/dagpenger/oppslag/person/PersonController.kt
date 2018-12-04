package no.nav.dagpenger.oppslag.person

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.person(personClient: PersonClientSoap) {
    post("api/person/geografisk-tilknytning") {
        val request = call.receive<GeografiskTilknytningRequest>()

        val geografiskTilknytningResponse = personClient.getGeografiskTilknytning(request.fødselsnummer)

        call.respond(geografiskTilknytningResponse)
    }
}

data class GeografiskTilknytningRequest(val fødselsnummer: String)