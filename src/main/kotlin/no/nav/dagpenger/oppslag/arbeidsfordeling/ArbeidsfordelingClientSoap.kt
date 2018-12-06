package no.nav.dagpenger.oppslag.arbeidsfordeling

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.ArbeidsfordelingKriterier
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Diskresjonskoder
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Geografi
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Organisasjonsenhet
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Tema
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest

class ArbeidsfordelingClientSoap(private val arbeidsFordeling: ArbeidsfordelingV1) {

    fun getBehandlendeEnhet(incomingRequest: BehandlendeEnhetRequest): List<Organisasjonsenhet> {
        val soapRequest = FinnBehandlendeEnhetListeRequest()

        soapRequest.arbeidsfordelingKriterier = ArbeidsfordelingKriterier().apply {
            this.geografiskTilknytning = Geografi().apply { value = incomingRequest.geografiskTilknytning }
            this.tema = Tema().apply { value = incomingRequest.tema }

            incomingRequest.diskresjonskode?.let {
                this.diskresjonskode = Diskresjonskoder().apply { value = incomingRequest.diskresjonskode }
            }
        }

        val response = arbeidsFordeling.finnBehandlendeEnhetListe(soapRequest)

        return response.behandlendeEnhetListe
    }
}
