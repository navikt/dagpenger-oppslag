package no.nav.dagpenger.oppslag.arena

import no.nav.arena.services.lib.sakvedtak.SaksInfo
import no.nav.arena.services.lib.sakvedtak.SaksInfoListe
import no.nav.arena.services.sakvedtakservice.Bruker
import no.nav.arena.services.sakvedtakservice.SakVedtakPortType
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.binding.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.Oppgave
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.Oppgavetype
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.Person
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.Prioritet
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.SakInfo
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.informasjon.Tema
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.meldinger.BestillOppgaveRequest
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.meldinger.BestillOppgaveResponse
import javax.xml.ws.Holder

class ArenaClientSoap(val oppgaveV1: BehandleArbeidOgAktivitetOppgaveV1, val hentsak: SakVedtakPortType) {
    fun createSak(behandlendeEnhetId: String, fødselsnummer: String): String {
        val request = BestillOppgaveRequest()

        request.oppgavetype = Oppgavetype().apply { value = "STARTVEDTAK" }

        request.oppgave = Oppgave().apply {
            tema = Tema().apply { value = "DAG" }
            prioritet = Prioritet().apply { value = "HOY" }
            bruker = Person().apply { ident = fødselsnummer }
            this.behandlendeEnhetId = behandlendeEnhetId
            // TODO: verify if frist is optional/has default value when creating sak in arena
            // frist = GregorianCalendar().let { DatatypeFactory.newInstance().newXMLGregorianCalendar(it) }
            sakInfo = SakInfo().apply {
                isTvingNySak = true
            }
        }

        val response: BestillOppgaveResponse = oppgaveV1.bestillOppgave(request)

        return response.arenaSakId
    }

    fun createOppgave(behandlendeEnhetId: String, fødselsnummer: String, sakId: String): String {
        val request = BestillOppgaveRequest()

        request.oppgavetype = Oppgavetype().apply { value = "BEHENVPERSON" }

        request.oppgave = Oppgave().apply {
            tema = Tema().apply { value = "DAG" }
            prioritet = Prioritet().apply { value = "HOY" }
            bruker = Person().apply { ident = fødselsnummer }
            this.behandlendeEnhetId = behandlendeEnhetId
            // TODO: verify if frist is optional/has default value when creating sak in arena
            // frist = GregorianCalendar().let { DatatypeFactory.newInstance().newXMLGregorianCalendar(it) }
            sakInfo = SakInfo().apply {
                arenaSakId = sakId
                isTvingNySak = false
            }
        }

        val response: BestillOppgaveResponse = oppgaveV1.bestillOppgave(request)

        return response.oppgaveId
    }

    fun getDagpengerSaker(fødselsnummer: String, brukerType: String): List<SaksInfo> {

        val bruker = Holder<Bruker>().apply {
            value = Bruker().apply {
                brukerId = fødselsnummer
                brukertypeKode = brukerType
            }
        }

        val saker = Holder<SaksInfoListe>()

        hentsak.hentSaksInfoListeV2(bruker, null, null, null, "DAG", false, saker)

        return saker.value.saksInfo
    }
}
