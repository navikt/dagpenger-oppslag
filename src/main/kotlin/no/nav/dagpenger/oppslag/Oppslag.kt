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
import no.nav.dagpenger.oppslag.arbeidsfordeling.ArbeidsfordelingClientSoap
import no.nav.dagpenger.oppslag.arbeidsfordeling.arbeidsfordeling
import no.nav.dagpenger.oppslag.arena.ArenaClientSoap
import no.nav.dagpenger.oppslag.arena.arena
import no.nav.dagpenger.oppslag.person.PersonClientSoap
import no.nav.dagpenger.oppslag.person.person
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.binding.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3

private val LOGGER = KotlinLogging.logger {}

private val username: String? = System.getenv("SRVDAGPENGER_OPPSLAG_USERNAME")
private val password: String? = System.getenv("SRVDAGPENGER_OPPSLAG_PASSWORD")
private val oicdStsUrl: String? = System.getenv("OIDC_STS_ISSUERURL")
private val dagpengerPersonUrl: String? = System.getenv("DAGPENGER_PERSON_URL")
private val dagpengerArbeidsfordelingUrl: String? = System.getenv("DAGPENGER_ARBEIDSFORDELING_URL")
private val dagpengerArenaOppgaveUrl: String? = System.getenv("DAGPENGER_ARENA_OPPGAVE_URL")

class Oppslag {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            embeddedServer(Netty, port = 8080, module = Application::main).start(wait = true)
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

    val person = WsClient<PersonV3>(oicdStsUrl, username, password)
            .createPortForSystemUser(dagpengerPersonUrl, PersonV3::class.java)
    val personClient = PersonClientSoap(person)

    val arbeidsfordeling = WsClient<ArbeidsfordelingV1>(oicdStsUrl, username, password)
            .createPortForSystemUser(dagpengerArbeidsfordelingUrl, ArbeidsfordelingV1::class.java)
    val arbeidsfordelingClient = ArbeidsfordelingClientSoap(arbeidsfordeling)

    val behandleArbeidOgAktivitetOppgave = WsClient<BehandleArbeidOgAktivitetOppgaveV1>(oicdStsUrl, username, password)
            .createPortForSystemUser(dagpengerArenaOppgaveUrl, BehandleArbeidOgAktivitetOppgaveV1::class.java)
    val arenaClient = ArenaClientSoap(behandleArbeidOgAktivitetOppgave)

    routing {
        person(personClient)
        arbeidsfordeling(arbeidsfordelingClient)
        arena(arenaClient)

        get("/isAlive") {
            call.respondText("ALIVE", ContentType.Text.Plain)
        }
        get("/isReady") {
            call.respondText("READY", ContentType.Text.Plain)
        }
    }
}