package no.nav.dagpenger.oppslag.ws.person

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.Success

fun Route.person(personClient: PersonClient) {
    post("api/person/geografisk-tilknytning") {
        val json = call.receive<GeografiskTilknytningRequest>()

        val oppslagResult = personClient.getGeografiskTilknytning(json.fødselsnummer)
        when (oppslagResult) {
            is Success<*> -> call.respond(oppslagResult.data!!)
            is Failure -> call.respond(HttpStatusCode.InternalServerError, "Error")
        }
    }

    post("api/person/name") {
        val request = call.receive<PersonNameRequest>()

        val oppslagResult = personClient.getName(request.fødselsnummer)

        when (oppslagResult) {
            is Success<*> -> call.respond(oppslagResult.data!!)
            is Failure -> call.respond(HttpStatusCode.InternalServerError, "Error")
        }
    }
}

data class GeografiskTilknytningRequest(val fødselsnummer: String)

data class PersonNameRequest(val fødselsnummer: String)
