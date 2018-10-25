package no.nav.dagpenger.oppslag.arbeidsfordeling

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.arbeidsfordeling(arbeidsfordelingClient: ArbeidsfordelingClientSoap) {

    post("behandlende-enhet") {
        val (geografiskTilknytning, diskresjonskode ) = call.receive<GeografiskTilknytningRequest>()

        val behandlendeEnhet = arbeidsfordelingClient.getBehandlendeEnhet(
                geografiskTilknytning,
                diskresjonskode)

        call.respond(behandlendeEnhet ?: "")
    }
}

data class GeografiskTilknytningRequest(
    val geografiskTilknytning: String,
    val diskresjonskode: String?
)