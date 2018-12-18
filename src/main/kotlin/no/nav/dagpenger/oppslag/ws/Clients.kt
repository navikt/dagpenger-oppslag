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
