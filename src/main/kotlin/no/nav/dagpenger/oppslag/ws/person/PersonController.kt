package no.nav.dagpenger.oppslag.ws.person

import io.ktor.application.call
import io.ktor.http.CacheControl
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.cacheControl
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.Success

private const val CACHE_SECONDS = 86400

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
            is Success<*> -> {
                call.response.cacheControl(CacheControl.MaxAge(CACHE_SECONDS))
                call.respond(oppslagResult.data!!)
            }
            is Failure -> call.respond(HttpStatusCode.InternalServerError, "Error")
        }
    }
}

data class GeografiskTilknytningRequest(val fødselsnummer: String)

data class PersonNameRequest(val fødselsnummer: String)
