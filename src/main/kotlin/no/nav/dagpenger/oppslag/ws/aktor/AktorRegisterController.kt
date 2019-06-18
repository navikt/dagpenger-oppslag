package no.nav.dagpenger.oppslag.ws.aktor

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.aktorRegister(aktorRegisterClient: AktorRegisterHttpClient) {
    route("api/naturlig-ident") {
        get {
            call.request.headers["ident"]?.let { ident ->
                aktorRegisterClient.gjeldendeNorskIdent(ident)?.let { naturligIdent ->
                    call.respond(HttpStatusCode.OK, NaturligIdentResponse(naturligIdent))
                } ?: call.respond(HttpStatusCode.NotFound, "Fant inget fødselsnummer for $ident")
            } ?: call.respond(HttpStatusCode.NotAcceptable, MissingHeader("ident"))
        }
    }
}

data class MissingHeader(val headerName: String)
data class NaturligIdentResponse(val naturligIdent: String)