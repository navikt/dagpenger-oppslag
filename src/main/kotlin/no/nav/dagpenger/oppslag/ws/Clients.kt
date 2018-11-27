package no.nav.dagpenger.oppslag.ws

import no.nav.arena.services.sakvedtakservice.SakVedtakPortType
import no.nav.dagpenger.oppslag.Environment
import no.nav.dagpenger.oppslag.ws.arbeidsfordeling.ArbeidsfordelingClientSoap
import no.nav.dagpenger.oppslag.ws.arena.ArenaClientSoap
import no.nav.dagpenger.oppslag.ws.joark.JoarkClientSoap
import no.nav.dagpenger.oppslag.ws.person.PersonClientSoap
import no.nav.dagpenger.oppslag.ws.sts.addSAMLTokenOnBehalfOfOidcToken
import no.nav.dagpenger.oppslag.ws.sts.configureFor
import no.nav.dagpenger.oppslag.ws.sts.stsClient
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.behandlearbeidogaktivitetoppgave.v1.binding.BehandleArbeidOgAktivitetOppgaveV1
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import org.apache.cxf.ext.logging.LoggingFeature
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean

import no.nav.dagpenger.oppslag.ws.sts.OnBehalfOfOutInterceptor

class Clients(env: Environment) {
    private val stsClient = stsClient(
        env.securityTokenServiceEndpointUrl,
        env.securityTokenUsername to env.securityTokenPassword
    )

    val personClient = PersonClientSoap(
        createServicePort(
            endpoint = env.personUrl,
            service = PersonV3::class.java
        ).apply(stsClient::configureFor)
    )

    val arbeidsfordelingClient = ArbeidsfordelingClientSoap(
        createServicePort(
            endpoint = env.arbeidsfordelingUrl,
            service = ArbeidsfordelingV1::class.java
        ).apply(stsClient::configureFor)
    )

    val arenaClient = ArenaClientSoap(
        createServicePort(
            endpoint = env.arenaOppgaveUrl,
            service = BehandleArbeidOgAktivitetOppgaveV1::class.java
        ).apply(stsClient::configureFor),
        createServicePort(
            endpoint = env.arenaHentSakerUrl,
            service = SakVedtakPortType::class.java
        ).apply {
            stsClient::configureFor
            stsClient.addSAMLTokenOnBehalfOfOidcToken(OnBehalfOfOutInterceptor())
        }
    )

    val joarkClient = JoarkClientSoap(
        createServicePort(
            endpoint = env.inngaaendeJournalUrl,
            service = BehandleInngaaendeJournalV1::class.java
        ).apply {
            stsClient::configureFor
            stsClient.addSAMLTokenOnBehalfOfOidcToken(OnBehalfOfOutInterceptor())
        }
    )

    private fun <PORT_TYPE> createServicePort(endpoint: String, service: Class<PORT_TYPE>): PORT_TYPE {
        val factory = JaxWsProxyFactoryBean().apply {
            address = endpoint
            serviceClass = service
            features = listOf(LoggingFeature())
        }

        @Suppress("UNCHECKED_CAST")
        return factory.create() as PORT_TYPE
    }
}