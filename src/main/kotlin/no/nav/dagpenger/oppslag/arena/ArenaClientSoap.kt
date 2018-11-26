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
    fun createOppgave(arenaOppgaveRequest: CreateArenaOppgaveRequest): String {
        val soapRequest = BestillOppgaveRequest()

        soapRequest.oppgavetype = Oppgavetype().apply { value = arenaOppgaveRequest.oppgaveType }
        soapRequest.oppgave = Oppgave().apply {
            tema = Tema().apply { value = arenaOppgaveRequest.tema }
            prioritet = Prioritet().apply { value = arenaOppgaveRequest.prioritet }
            bruker = Person().apply { ident = arenaOppgaveRequest.fødselsnummer }
            this.behandlendeEnhetId = arenaOppgaveRequest.behandlendeEnhetId
            // TODO: verify if frist is optional/has default value when creating sak in arena
            // frist = GregorianCalendar().let { DatatypeFactory.newInstance().newXMLGregorianCalendar(it) }
            sakInfo = SakInfo().apply {
                arenaSakId = arenaOppgaveRequest.sakId
                isTvingNySak = arenaOppgaveRequest.tvingNySak
            }
        }

        val response: BestillOppgaveResponse = oppgaveV1.bestillOppgave(soapRequest)

        return response.arenaSakId
    }

    fun getDagpengerSaker(getArenaSakerRequest: GetArenaSakerRequest): List<SaksInfo> {

        val bruker = Holder<Bruker>().apply {
            value = Bruker().apply {
                brukerId = getArenaSakerRequest.fødselsnummer
                brukertypeKode = getArenaSakerRequest.brukerType
            }
        }

        val saker = Holder<SaksInfoListe>()

        hentsak.hentSaksInfoListeV2(bruker, null, null, null, getArenaSakerRequest.tema, getArenaSakerRequest.includeInactive, saker)

        return saker.value.saksInfo
    }
}
