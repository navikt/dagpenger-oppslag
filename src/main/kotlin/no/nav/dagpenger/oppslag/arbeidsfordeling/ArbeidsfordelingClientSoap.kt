package no.nav.dagpenger.oppslag.arbeidsfordeling

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.ArbeidsfordelingKriterier
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Diskresjonskoder
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Geografi
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Organisasjonsenhet
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest

class ArbeidsfordelingClientSoap(private val arbeidsFordeling: ArbeidsfordelingV1) {

    fun getBehandlendeEnhet(geografiskTilknytning: String, diskresjonskode: String?): List<Organisasjonsenhet> {
        val request = FinnBehandlendeEnhetListeRequest()

        request.arbeidsfordelingKriterier = ArbeidsfordelingKriterier().apply {
            diskresjonskode?.let {
                this.diskresjonskode = Diskresjonskoder().apply { kodeverksRef = diskresjonskode }
            }
            this.geografiskTilknytning = Geografi().apply { kodeverksRef = geografiskTilknytning }
        }

        val response = arbeidsFordeling.finnBehandlendeEnhetListe(request)

        return response.behandlendeEnhetListe
    }
}
