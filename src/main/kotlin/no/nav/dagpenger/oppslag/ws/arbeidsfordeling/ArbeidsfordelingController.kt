package no.nav.dagpenger.oppslag.ws.arbeidsfordeling

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.arbeidsfordeling(arbeidsfordelingClient: ArbeidsfordelingClientSoap) {
    post("api/arbeidsfordeling/behandlende-enhet") {

        val request = call.receive<BehandlendeEnhetRequest>()

        val behandlendeEnheter = arbeidsfordelingClient.getBehandlendeEnhet(request)
            .map { organisasjonsenhet ->
                BehandlendeEnhet(organisasjonsenhet.enhetId, organisasjonsenhet.enhetNavn)
            }

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
