package no.nav.dagpenger.oppslag

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging
import no.nav.arena.services.sakvedtakservice.SakVedtakPortType
import no.nav.dagpenger.oppslag.arbeidsfordeling.ArbeidsfordelingClientSoap
import no.nav.dagpenger.oppslag.arbeidsfordeling.arbeidsfordeling
import no.nav.dagpenger.oppslag.arena.ArenaClientSoap
import no.nav.dagpenger.oppslag.arena.arena
import no.nav.dagpenger.oppslag.joark.JoarkClientSoap
import no.nav.dagpenger.oppslag.joark.joark
import no.nav.dagpenger.oppslag.person.PersonClientSoap
import no.nav.dagpenger.oppslag.person.person
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.binding.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3

private val LOGGER = KotlinLogging.logger {}

class Oppslag {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val env = Environment()
            embeddedServer(Netty, port = env.httpPort, module = Application::main).start(wait = true)
        }
    }
}

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    val env = Environment()

    val person = WsClient<PersonV3>(env.oicdStsUrl, env.username, env.password)
            .createPortForSystemUser(env.dagpengerPersonUrl, PersonV3::class.java)
    val personClient = PersonClientSoap(person)

    val arbeidsfordeling = WsClient<ArbeidsfordelingV1>(env.oicdStsUrl, env.username, env.password)
            .createPortForSystemUser(env.dagpengerArbeidsfordelingUrl, ArbeidsfordelingV1::class.java)
    val arbeidsfordelingClient = ArbeidsfordelingClientSoap(arbeidsfordeling)

    val behandleArbeidOgAktivitetOppgave = WsClient<BehandleArbeidOgAktivitetOppgaveV1>(env.oicdStsUrl, env.username, env.password)
            .createPortForSystemUser(env.dagpengerArenaOppgaveUrl, BehandleArbeidOgAktivitetOppgaveV1::class.java)

    val hentSaksInfoListe = WsClient<SakVedtakPortType>(env.oicdStsUrl, env.username, env.password)
            .createPortForSystemUser(env.dagpengerArenaHentSakerUrl, SakVedtakPortType::class.java)
    val arenaClient = ArenaClientSoap(behandleArbeidOgAktivitetOppgave, hentSaksInfoListe)

    val inngåendeJournal = WsClient<BehandleInngaaendeJournalV1>(env.oicdStsUrl, env.username, env.password)
            .createPortForSystemUser(env.dagpengerInngaaendeJournalUrl, BehandleInngaaendeJournalV1::class.java)
    val joarkClient = JoarkClientSoap(inngåendeJournal)

    routing {
        person(personClient)
        arbeidsfordeling(arbeidsfordelingClient)
        arena(arenaClient)
        joark(joarkClient)

        get("/isAlive") {
            call.respondText("ALIVE", ContentType.Text.Plain)
        }
        get("/isReady") {
            call.respondText("READY", ContentType.Text.Plain)
        }
    }
}