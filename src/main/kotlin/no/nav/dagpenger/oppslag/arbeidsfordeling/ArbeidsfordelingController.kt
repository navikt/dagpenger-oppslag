package no.nav.dagpenger.oppslag.arbeidsfordeling

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

fun Routing.arbeidsfordeling(arbeidsfordelingClient: ArbeidsfordelingClientSoap) {

    post("api/arbeidsfordeling/behandlende-enhet") {
        val request = call.receive<BehandlendeEnhetRequest>()

        val behandlendeEnheter = arbeidsfordelingClient.getBehandlendeEnhet(request)
                .map { it -> BehandlendeEnhet(it.enhetId, it.enhetNavn) }

        call.respond(BehandlendeEnhetResponse(behandlendeEnheter))
    }
}

data class BehandlendeEnhetRequest(
    val geografiskTilknytning: String,
    val tema: String,
    val diskresjonskode: String?
)

data class BehandlendeEnhet(
    var enhetId: String,
    var enhetNavn: String
)

data class BehandlendeEnhetResponse(val behandlendeEnheter: List<BehandlendeEnhet>)
