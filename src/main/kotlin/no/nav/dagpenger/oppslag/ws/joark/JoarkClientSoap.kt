package no.nav.dagpenger.oppslag.ws.joark

import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.FerdigstillJournalfoeringRequest

class JoarkClientSoap(private val inngåendeJournal: BehandleInngaaendeJournalV1) {
    fun ferdigstillJournalføring(journalpostId: String) {
        val request = FerdigstillJournalfoeringRequest()
        request.journalpostId = journalpostId

        inngåendeJournal.ferdigstillJournalfoering(request)
    }
}
