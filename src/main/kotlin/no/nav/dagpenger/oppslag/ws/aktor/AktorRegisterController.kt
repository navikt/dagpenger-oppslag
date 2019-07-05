package no.nav.dagpenger.oppslag.ws.aktor

import io.ktor.application.call
import io.ktor.http.CacheControl
import io.ktor.http.HttpStatusCode
import io.ktor.response.cacheControl
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

private const val CACHE_SECONDS = 86400

fun Route.aktorRegister(aktorRegisterClient: AktorRegisterHttpClient) {
    route("api/naturlig-ident") {
        get {
            call.request.headers["ident"]?.let { ident ->
                aktorRegisterClient.gjeldendeNorskIdent(ident)?.let { naturligIdent ->
                    call.response.cacheControl(CacheControl.MaxAge(CACHE_SECONDS))
                    call.respond(HttpStatusCode.OK, NaturligIdentResponse(naturligIdent))
                } ?: call.respond(HttpStatusCode.NotFound, "Fant inget fødselsnummer for $ident")
            } ?: call.respond(HttpStatusCode.NotAcceptable, MissingHeader("ident"))
        }
    }
}

data class MissingHeader(val headerName: String)
data class NaturligIdentResponse(val naturligIdent: String)