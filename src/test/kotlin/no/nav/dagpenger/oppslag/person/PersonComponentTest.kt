package no.nav.dagpenger.oppslag.person

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
import no.nav.dagpenger.oppslag.JwtStub
import no.nav.dagpenger.oppslag.oppslag
import no.nav.dagpenger.oppslag.samlAssertionResponse
import no.nav.dagpenger.oppslag.stsStub
import no.nav.dagpenger.oppslag.withCallId
import no.nav.dagpenger.oppslag.withSamlAssertion
import no.nav.dagpenger.oppslag.withSoapAction
import no.nav.dagpenger.oppslag.ws.SoapPort
import no.nav.dagpenger.oppslag.ws.aktor.AktorRegisterHttpClient
import no.nav.dagpenger.oppslag.ws.brreg.enhetsregister.EnhetsRegisteretHttpClient
import no.nav.dagpenger.oppslag.ws.joark.JoarkClient
import no.nav.dagpenger.oppslag.ws.person.PersonClient
import no.nav.dagpenger.oppslag.ws.sts.STS_SAML_POLICY_NO_TRANSPORT_BINDING
import no.nav.dagpenger.oppslag.ws.sts.configureFor
import no.nav.dagpenger.oppslag.ws.sts.stsClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PersonComponentTest {

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

    @Test
    fun `geografiskTilknytning api response is valid json`() {
        val jwtStub = JwtStub("test issuer")
        val token = jwtStub.createTokenFor("srvdp-jrnf-ruting")

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
            personServiceStub("08078422069")
                .withSamlAssertion(
                    "testusername", "theIssuer", "CN=B27 Issuing CA Intern, DC=preprod, DC=local",
                    "digestValue", "signatureValue", "certificateValue"
                )
                .withCallId()
                .willReturn(WireMock.ok(hentGeografiskTilknytning_response))
        )

        val securityTokenServiceEndpointUrl = server.baseUrl().plus("/sts")
        val securityTokenUsername = "stsUsername"
        val securityTokenPassword = "stsPassword"
        val personUrl = server.baseUrl().plus("/person")
        val jwtIssuer = "test issuer"
        val allowInsecureSoapRequests = true

        val stsClient by lazy {
            stsClient(
                securityTokenServiceEndpointUrl,
                securityTokenUsername to securityTokenPassword
            )
        }

        val joarkClient: JoarkClient = mockk()
        val aktorRegisterHttpClient = mockk<AktorRegisterHttpClient>()
        val enhetsRegisterClient = mockk<EnhetsRegisteretHttpClient>()

        val personPort = SoapPort.PersonV3(personUrl)

        if (allowInsecureSoapRequests) {
            stsClient.configureFor(personPort, STS_SAML_POLICY_NO_TRANSPORT_BINDING)
        } else {
            stsClient.configureFor(personPort)
        }

        val personClient = PersonClient(personPort)

        withTestApplication({
            oppslag(
                jwkProvider = jwtStub.stubbedJwkProvider(),
                jwtIssuer = jwtIssuer,
                joarkClient = joarkClient,
                personClient = personClient,
                aktorRegisterClient = aktorRegisterHttpClient,
                enhetRegisterClient = enhetsRegisterClient
            )
        }) {
            handleRequest(HttpMethod.Post, "api/person/geografisk-tilknytning") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody("{\"fødselsnummer\": \"08078422069\"}")
            }.apply {
                Assertions.assertEquals(200, response.status()?.value)
                Assertions.assertEquals(
                    "{\"geografiskTilknytning\":\"en geografisk tilknytning\",\"diskresjonskode\":\"1\"}",
                    response.content
                )
            }
        }
    }
}

private fun personServiceStub(ident: String): MappingBuilder {
    return WireMock.post(WireMock.urlPathEqualTo("/person"))
        .withSoapAction("http://nav.no/tjeneste/virksomhet/person/v3/Person_v3/hentGeografiskTilknytningRequest")
        .withRequestBody(
            MatchesXPathPattern(
                "//soap:Envelope/soap:Body/ns2:hentGeografiskTilknytning/request/aktoer/ident/ident/text()",
                personNamespace, WireMock.equalTo(ident)
            )
        )
}

private val personNamespace = mapOf(
    "soap" to "http://schemas.xmlsoap.org/soap/envelope/",
    "ns2" to "http://nav.no/tjeneste/virksomhet/person/v3",
    "ns3" to "http://nav.no/tjeneste/virksomhet/person/v3/informasjon"
)

val hentGeografiskTilknytning_response =
    """
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:v3="http://nav.no/tjeneste/virksomhet/person/v3" xmlns:ns3="http://nav.no/tjeneste/virksomhet/person/v3/informasjon">
	<soapenv:Header/>
	<soapenv:Body>
		<v3:hentGeografiskTilknytningResponse>
			<response>
				<geografiskTilknytning xsi:type="ns3:Kommune" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
					<geografiskTilknytning>en geografisk tilknytning</geografiskTilknytning>
				</geografiskTilknytning>
                <diskresjonskode kodeRef="?" kodeverksRef="http://nav.no/kodeverk/Kodeverk/Diskresjonskoder">1</diskresjonskode>
			</response>
		</v3:hentGeografiskTilknytningResponse>
	</soapenv:Body>
</soapenv:Envelope>
""".trimIndent()
