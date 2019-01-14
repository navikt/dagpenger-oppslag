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
import no.nav.dagpenger.oppslag.Environment
import no.nav.dagpenger.oppslag.JwtStub
import no.nav.dagpenger.oppslag.oppslag
import no.nav.dagpenger.oppslag.samlAssertionResponse
import no.nav.dagpenger.oppslag.stsStub
import no.nav.dagpenger.oppslag.withCallId
import no.nav.dagpenger.oppslag.withSamlAssertion
import no.nav.dagpenger.oppslag.withSoapAction
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

//    @AfterEach
//    fun `clear prometheus registry`() {
//        CollectorRegistry.defaultRegistry.clear()
//    }

    @Test
    fun `that response is json`() {
        val jwtStub = JwtStub("test issuer")
        val token = jwtStub.createTokenFor("srvdp-jrnf-ruting")

        WireMock.stubFor(stsStub("stsUsername", "stsPassword")
            .willReturn(samlAssertionResponse("testusername", "theIssuer", "CN=B27 Issuing CA Intern, DC=preprod, DC=local",
                "digestValue", "signatureValue", "certificateValue")))

        WireMock.stubFor(personServiceStub("08078422069")
            .withSamlAssertion("testusername", "theIssuer", "CN=B27 Issuing CA Intern, DC=preprod, DC=local",
                "digestValue", "signatureValue", "certificateValue")
            .withCallId("Sett inn call id her")
            .willReturn(WireMock.ok(hentGeografiskTilknytning_response)))

        val env = Environment(mapOf(
            "SECURITYTOKENSERVICE_URL" to server.baseUrl().plus("/sts"),
            "SRVDAGPENGER_OPPSLAG_USERNAME" to "stsUsername",
            "SRVDAGPENGER_OPPSLAG_PASSWORD" to "stsPassword",
            "VIRKSOMHET_PERSON_V3_ENDPOINTURL" to server.baseUrl().plus("/person"),
            "JWT_ISSUER" to "test issuer",
            "ALLOW_INSECURE_SOAP_REQUESTS" to "true"
        ))

        withTestApplication({ oppslag(env, jwtStub.stubbedJwkProvider()) }) {
            handleRequest(HttpMethod.Post, "api/person/geografisk-tilknytning") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody("{\"fødselsnummer\": \"08078422069\"}")
            }.apply {
                Assertions.assertEquals(200, response.status()?.value)
                Assertions.assertEquals("{\"geografiskTilknytning\":\"en geografisk tilknytning\",\"diskresjonskode\":\"1\"}", response.content)
            }
        }
    }
}

fun personServiceStub(ident: String): MappingBuilder {
    return WireMock.post(WireMock.urlPathEqualTo("/person"))
        .withSoapAction("http://nav.no/tjeneste/virksomhet/person/v3/Person_v3/hentGeografiskTilknytningRequest")
        .withRequestBody(
            MatchesXPathPattern("//soap:Envelope/soap:Body/ns2:hentGeografiskTilknytning/request/aktoer/ident/ident/text()",
            personNamespace, WireMock.equalTo(ident))
        )
}

private val personNamespace = mapOf(
    "soap" to "http://schemas.xmlsoap.org/soap/envelope/",
    "ns2" to "http://nav.no/tjeneste/virksomhet/person/v3",
    "ns3" to "http://nav.no/tjeneste/virksomhet/person/v3/informasjon"
)

val hentGeografiskTilknytning_response = """
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
