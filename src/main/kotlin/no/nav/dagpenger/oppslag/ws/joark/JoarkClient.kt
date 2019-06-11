package no.nav.dagpenger.oppslag.ws.joark

import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.OppslagResult
import org.slf4j.LoggerFactory

// TODO : Remimplement to rest service https://confluence.adeo.no/display/BOA/ferdigstillJournalpost
// https://confluence.adeo.no/pages/viewpage.action?pageId=233387943 is legacy
class JoarkClient(private val inngåendeJournal: String) {
//    private val counter = Counter.build()
//        .name("ferdigstill_journalpost")
//        .labelNames("status")
//        .help("Antall ferdigstillinger av Journalposter")
//        .register()

    private val log = LoggerFactory.getLogger("PersonClient")

    fun ferdigstillJournalføring(journalpostId: String): OppslagResult {

        return try {
            TODO("NOT IMPLEMENTED")
//            counter.labels("success").inc()
        } catch (ex: Exception) {
            log.error("Error while calling ferdigstillJournalfoering", ex)
//            counter.labels("failure").inc()
            Failure(listOf(ex.message ?: "unknown error"))
        }
    }
}
