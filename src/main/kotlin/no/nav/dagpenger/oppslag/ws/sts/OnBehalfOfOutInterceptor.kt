package no.nav.dagpenger.oppslag.ws.sts

import org.apache.cxf.message.Message
import org.apache.cxf.phase.AbstractPhaseInterceptor
import org.apache.cxf.phase.Phase
import org.apache.cxf.rt.security.SecurityConstants.STS_TOKEN_ON_BEHALF_OF
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import java.util.Base64
import javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class OnBehalfOfOutInterceptor : AbstractPhaseInterceptor<Message>(Phase.SETUP) {
    private val OIDC_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:jwt"

    override fun handleMessage(message: Message?) {
        //val token = tokenHandler.getToken()
        var token = "123"
        message?.set(STS_TOKEN_ON_BEHALF_OF, createOnBehalfOfElement(token))
    }

    private fun createOnBehalfOfElement(token: String): Element {
        try {
            val content = wrapWithBinarySecurityToken(token.toByteArray())
            val factory = DocumentBuilderFactory.newInstance()

            factory.isNamespaceAware = true
            factory.setFeature(FEATURE_SECURE_PROCESSING, true)

            return factory.newDocumentBuilder().parse(InputSource(StringReader(content))).documentElement
        } catch (e: ParserConfigurationException) {
            throw RuntimeException(e)
        } catch (e: SAXException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun wrapWithBinarySecurityToken(token: ByteArray): String {
        val base64encodedToken = Base64.getEncoder().encodeToString(token)

        return """<wsse:BinarySecurityToken xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
            |EncodingType="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary"
            |ValueType="${OnBehalfOfOutInterceptor::OIDC_TOKEN_TYPE}">$base64encodedToken</wsse:BinarySecurityToken>""".trimMargin()
    }
}