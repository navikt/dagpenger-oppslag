package no.nav.dagpenger.oppslag.joark

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.MatchesXPathPattern
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.mockk
import no.nav.dagpenger.oppslag.Environment
import no.nav.dagpenger.oppslag.JwtStub
import no.nav.dagpenger.oppslag.oppslag
import no.nav.dagpenger.oppslag.samlAssertionResponse
import no.nav.dagpenger.oppslag.stsStub
import no.nav.dagpenger.oppslag.withCallId
import no.nav.dagpenger.oppslag.withSamlAssertion
import no.nav.dagpenger.oppslag.withSoapAction
import no.nav.dagpenger.oppslag.ws.Clients
import no.nav.dagpenger.oppslag.ws.joark.JoarkClient
import no.nav.dagpenger.oppslag.ws.person.PersonClientSoap
import no.nav.dagpenger.oppslag.ws.sts.STS_SAML_POLICY_NO_TRANSPORT_BINDING
import no.nav.dagpenger.oppslag.ws.sts.configureFor
import no.nav.dagpenger.oppslag.ws.sts.stsClient
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JoarkComponentTest {

    companion object {
        val server: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())

        @BeforeAll
        @JvmStatic
        fun start() {
            server.start()
        }

        @AfterAll
        @JvmStatic
        fun stop() {
            server.stop()
        }
    }

    @BeforeEach
    fun configure() {
        WireMock.configureFor(server.port())
    }

//    @AfterEach
//    fun `clear prometheus registry`() {
//        CollectorRegistry.defaultRegistry.clear()
//    }

    @Test
    fun `that response is json`() {
        val jwtStub = JwtStub("test issuer")
        val token = jwtStub.createTokenFor("srvdp-jrnf-ferdig")

        WireMock.stubFor(
            stsStub("stsUsername", "stsPassword")
                .willReturn(
                    samlAssertionResponse(
                        "testusername", "theIssuer", "CN=B27 Issuing CA Intern, DC=preprod, DC=local",
                        "digestValue", "signatureValue", "certificateValue"
                    )
                )
        )

        WireMock.stubFor(
            joarkServiceStub("08078422069")
                .withSamlAssertion(
                    "testusername", "theIssuer", "CN=B27 Issuing CA Intern, DC=preprod, DC=local",
                    "digestValue", "signatureValue", "certificateValue"
                )
                .withCallId("Sett inn call id her")
                .willReturn(WireMock.ok(ferdigstillJournalfoering_response))
        )

        val env = Environment(
            mapOf(
                "SECURITYTOKENSERVICE_URL" to server.baseUrl().plus("/sts"),
                "SRVDAGPENGER_OPPSLAG_USERNAME" to "stsUsername",
                "SRVDAGPENGER_OPPSLAG_PASSWORD" to "stsPassword",
                "BEHANDLEINNGAAENDEJOURNAL_V1_ENDPOINTURL" to server.baseUrl().plus("/joark"),
                "JWT_ISSUER" to "test issuer",
                "ALLOW_INSECURE_SOAP_REQUESTS" to "true"
            )
        )

        val stsClient by lazy {
            stsClient(
                env.securityTokenServiceEndpointUrl,
                env.securityTokenUsername to env.securityTokenPassword
            )
        }

        val joarkPort = Clients.createServicePort(
            endpoint = env.inngaaendeJournalUrl,
            service = BehandleInngaaendeJournalV1::class.java
        )

        if (env.allowInsecureSoapRequests) {
            stsClient.configureFor(joarkPort, STS_SAML_POLICY_NO_TRANSPORT_BINDING)
        } else {
            stsClient.configureFor(joarkPort)
        }

        val joarkClient = JoarkClient(joarkPort)

        val personClient: PersonClientSoap = mockk()

        withTestApplication({ oppslag(env, jwtStub.stubbedJwkProvider(), joarkClient, personClient) }) {
            handleRequest(HttpMethod.Post, "api/joark/ferdigstill") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody("{\"journalpostId\": \"08078422069\"}")
            }.apply {
                Assertions.assertEquals(200, response.status()?.value)
            }
        }
    }
}

fun joarkServiceStub(journalpostId: String): MappingBuilder {
    return WireMock.post(WireMock.urlPathEqualTo("/joark"))
        .withSoapAction("http://nav.no/tjeneste/virksomhet/behandleInngaaendeJournal/v1/BehandleInngaaendeJournal_v1/ferdigstillJournalfoeringRequest")
        .withRequestBody(
            MatchesXPathPattern(
                "//soap:Envelope/soap:Body/ns2:ferdigstillJournalfoering/request/journalpostId/text()",
                joarkNameSpace, WireMock.equalTo(journalpostId)
            )
        )
}

private val joarkNameSpace = mapOf(
    "soap" to "http://schemas.xmlsoap.org/soap/envelope/",
    "ns2" to "http://nav.no/tjeneste/virksomhet/behandleInngaaendeJournal/v1",
    "ns3" to "http://nav.no/tjeneste/virksomhet/behandleInngaaendeJournal/v1/informasjon"
)

val ferdigstillJournalfoering_response = """
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:v1="http://nav.no/tjeneste/virksomhet/behandleInngaaendeJournal/v1">
	<soapenv:Header/>
	<soapenv:Body>
		<v1:ferdigstillJournalfoeringResponse>
		</v1:ferdigstillJournalfoeringResponse>
	</soapenv:Body>
</soapenv:Envelope>
""".trimIndent()
