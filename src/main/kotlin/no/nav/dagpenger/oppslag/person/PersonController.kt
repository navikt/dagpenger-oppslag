package no.nav.dagpenger.oppslag.person

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.person(personClient: PersonClientSoap) {
    get("geografisk-tilknytning") {
        val fødelsnummer = "12345678910"

        val geografiskTilknytningResponse = personClient.getGeografiskTilknytning(fødelsnummer)

        call.respond(geografiskTilknytningResponse)
    }
}