package no.nav.dagpenger.oppslag.ws.arbeidsfordeling

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.arbeidsfordeling(arbeidsfordelingClient: ArbeidsfordelingClientSoap) {
    post("api/arbeidsfordeling/behandlende-enhet") {
        val (geografiskTilknytning, diskresjonskode) = call.receive<BehandlendeEnhetRequest>()
        val behandlendeEnhet = arbeidsfordelingClient.getBehandlendeEnhet(
            geografiskTilknytning,
            diskresjonskode
        )

        call.respond(behandlendeEnhet ?: "")
    }
}

data class BehandlendeEnhetRequest(
    val geografiskTilknytning: String,
    val diskresjonskode: String?
)