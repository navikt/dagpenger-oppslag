package no.nav.dagpenger.oppslag.arbeidsfordeling

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.ArbeidsfordelingKriterier
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Diskresjonskoder
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Geografi
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest

class ArbeidsfordelingClientSoap(private val arbeidsFordeling: ArbeidsfordelingV1) {

    fun getBehandlendeEnhet(geografiskTilknytning: String, diskresjonskode: String?): String? {
        val request = FinnBehandlendeEnhetListeRequest()
        val arbeidfordelingsKriterier = ArbeidsfordelingKriterier()

        diskresjonskode?.let {
            val diskresjonskoder = Diskresjonskoder()
            diskresjonskoder.setKodeverksRef(it)
            arbeidfordelingsKriterier.setDiskresjonskode(diskresjonskoder)
        }

        val geografi = Geografi()
        geografi.setKodeverksRef(geografiskTilknytning)
        arbeidfordelingsKriterier.setGeografiskTilknytning(geografi)

        val response = arbeidsFordeling.finnBehandlendeEnhetListe(request)
        val behandlendeEnhet = response.behandlendeEnhetListe.minBy { it.enhetId }

        return behandlendeEnhet?.enhetId
    }
}