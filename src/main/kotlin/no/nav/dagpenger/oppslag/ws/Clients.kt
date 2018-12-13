package no.nav.dagpenger.oppslag.ws

import org.apache.cxf.ext.logging.LoggingFeature
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean

object Clients {
    fun <PORT_TYPE> createServicePort(endpoint: String, service: Class<PORT_TYPE>): PORT_TYPE {
        val factory = JaxWsProxyFactoryBean().apply {
            address = endpoint
            serviceClass = service
            features = listOf(LoggingFeature())
            outInterceptors.add(CallIdInterceptor())
        }

        @Suppress("UNCHECKED_CAST")
        return factory.create() as PORT_TYPE
    }
}

/*class Clients(env: Environment) {
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
}*/