package no.nav.dagpenger.oppslag.arena

import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.binding.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.Oppgave
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.Oppgavetype
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.Person
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.Tema
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.meldinger.BestillOppgaveRequest
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.meldinger.BestillOppgaveResponse
import java.util.Calendar
import java.util.GregorianCalendar
import javax.xml.datatype.DatatypeFactory

class ArenaClientSoap(val oppgaveV1: BehandleArbeidOgAktivitetOppgaveV1) {
    fun bestillOppgave(behandlendeEnhetId: String, fødselsnummer: String): String {
        val request = BestillOppgaveRequest()

        request.oppgavetype = Oppgavetype().apply { value = "STARTVEDTAK" }

        request.oppgave = Oppgave().apply {
            tema = Tema().apply { value = "DAG" }
            bruker = Person().apply { ident = fødselsnummer }
            this.behandlendeEnhetId = behandlendeEnhetId
            //TODO: find out what to set as deadline/frist
            frist = GregorianCalendar().also { it.add(Calendar.DATE, 1) }.let { DatatypeFactory.newInstance().newXMLGregorianCalendar(it) }
        }

        val response: BestillOppgaveResponse = oppgaveV1.bestillOppgave(request)

        return response.arenaSakId
    }
}
