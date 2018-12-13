package no.nav.dagpenger.oppslag.ws.joark

import io.prometheus.client.Counter
import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.OppslagResult
import no.nav.dagpenger.oppslag.Success
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.FerdigstillJournalfoeringRequest
import org.slf4j.LoggerFactory

class JoarkClient(private val inngåendeJournal: BehandleInngaaendeJournalV1) {
    private val counter = Counter.build()
        .name("ferdigstill_journalpost")
        .labelNames("status")
        .help("Antall ferdigstillinger av Journalposter")
        .register()

    private val log = LoggerFactory.getLogger("PersonClient")

    fun ferdigstillJournalføring(journalpostId: String): OppslagResult {
        val request = FerdigstillJournalfoeringRequest().apply {
            journalpostId
        }

        return try {
            inngåendeJournal.ferdigstillJournalfoering(request)
            counter.labels("success").inc()
            Success(null)
        } catch (ex: Exception) {
            log.error("Error while calling ferdigstillJournalfoering", ex)
            counter.labels("failure").inc()
            Failure(listOf(ex.message ?: "unknown error"))
        }
    }
}
