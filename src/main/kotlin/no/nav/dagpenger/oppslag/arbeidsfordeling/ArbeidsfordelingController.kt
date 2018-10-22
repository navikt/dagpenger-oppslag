package no.nav.dagpenger.oppslag.arbeidsfordeling

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.arbeidsfordeling(arbeidsfordelingClient: ArbeidsfordelingClientSoap) {

    get("behandlende-enhet") {
        val geografiskTilknytningRequest = GeografiskTilknytningRequest(
                "test",
                null)

        val geografiskTilknytning = arbeidsfordelingClient.getBehandlendeEnhet(
                geografiskTilknytningRequest.geografiskTilknytning,
                geografiskTilknytningRequest.diskresjonskode)

        call.respond(geografiskTilknytning ?: "")
    }
}

data class GeografiskTilknytningRequest(
    val geografiskTilknytning: String,
    val diskresjonskode: String?
)