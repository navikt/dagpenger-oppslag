package no.nav.dagpenger.oppslag.person

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.person(personClient: PersonClient) {
    get("geografisk-tilknytning") {
        val fødelsnummer = "12345678910"

        val geografiskTilknytning = personClient.getGeografiskTilknytning(fødelsnummer)

        call.respondText(geografiskTilknytning)
    }
}