package no.nav.dagpenger.oppslag.arbeidsfordeling

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.arbeidsfordeling(arbeidsfordelingClient: ArbeidsfordelingClientSoap) {

    post("api/arbeidsfordeling/behandlende-enhet") {
        val (geografiskTilknytning, diskresjonskode ) = call.receive<BehandlendeEnhetRequest>()

        val behandlendeEnhet = arbeidsfordelingClient.getBehandlendeEnhet(
                geografiskTilknytning,
                diskresjonskode)

        call.respond(BehandlendeEnhetResponse(behandlendeEnhet))
    }
}

data class BehandlendeEnhetRequest(
    val geografiskTilknytning: String,
    val diskresjonskode: String?
)

data class BehandlendeEnhetResponse(val behandlendeEnhet: String)
