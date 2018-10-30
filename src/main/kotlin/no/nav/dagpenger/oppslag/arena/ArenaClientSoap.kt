package no.nav.dagpenger.oppslag.arena

import no.nav.arena.services.lib.sakvedtak.SaksInfo
import no.nav.arena.services.lib.sakvedtak.SaksInfoListe
import no.nav.arena.services.sakvedtakservice.Bruker
import no.nav.arena.services.sakvedtakservice.SakVedtakPortType
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.binding.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.*
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.meldinger.BestillOppgaveRequest
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.meldinger.BestillOppgaveResponse
import java.util.Calendar
import java.util.GregorianCalendar
import javax.xml.datatype.DatatypeFactory
import javax.xml.ws.Holder

class ArenaClientSoap(val oppgaveV1: BehandleArbeidOgAktivitetOppgaveV1, val hentsak: SakVedtakPortType) {
    fun bestillOppgave(behandlendeEnhetId: String, fødselsnummer: String): String {
        val request = BestillOppgaveRequest()

        request.oppgavetype = Oppgavetype().apply { value = "STARTVEDTAK" }

        request.oppgave = Oppgave().apply {
            tema = Tema().apply { value = "DAG" }
            prioritet = Prioritet().apply { kodeRef = "2" } //TODO: sjekk
            bruker = Person().apply { ident = fødselsnummer }
            this.behandlendeEnhetId = behandlendeEnhetId
            //TODO: find out what to set as deadline/frist
            frist = GregorianCalendar().also { it.add(Calendar.DATE, 1) }.let { DatatypeFactory.newInstance().newXMLGregorianCalendar(it) }
        }

        val response: BestillOppgaveResponse = oppgaveV1.bestillOppgave(request)

        return response.arenaSakId
    }

    fun getDagpengerSaker(fødselsnummer: String, brukerType: String, includeClosedSaker: Boolean) : List<SaksInfo> {

        val brukerHolder = Holder<Bruker>().apply {
            value = Bruker().apply {
                brukerId = fødselsnummer
                brukertypeKode = brukerType
            }
        }

        val sakerHolder = Holder<SaksInfoListe>()

        hentsak.hentSaksInfoListeV2(brukerHolder, null, null, null, "DAG", includeClosedSaker, sakerHolder)

        return sakerHolder.value.saksInfo
    }
}
