package no.nav.dagpenger.oppslag.ws.inntekt

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.OppslagResult
import no.nav.dagpenger.oppslag.Success

fun Route.inntekt(factory: () -> InntektClient) {
    val inntektClient by lazy(factory)

    post("api/inntekt/inntekt-liste") {
        call.receive<InntektRequest>().let { req ->
            val lookupResult: OppslagResult = inntektClient.hentInntektListe(req.aktorid)
            when (lookupResult) {
                is Success<*> -> call.respond(lookupResult.data!!)
                is Failure -> call.respond(HttpStatusCode.InternalServerError, "Failed get inntekt-liste")
            }
        }
    }
}

data class InntektRequest(val aktorid: String)