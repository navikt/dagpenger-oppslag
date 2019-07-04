package no.nav.dagpenger.oppslag.ws.brreg.enhetsregister

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.mockk
import no.nav.dagpenger.oppslag.Environment
import no.nav.dagpenger.oppslag.JwtStub
import no.nav.dagpenger.oppslag.oppslag
import no.nav.dagpenger.oppslag.ws.aktor.AktorRegisterHttpClient
import no.nav.dagpenger.oppslag.ws.joark.JoarkClient
import no.nav.dagpenger.oppslag.ws.person.PersonClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EnhetsRegisterApiTest {
    val joarkClientSoapMock = mockk<JoarkClient>()
    val personClientMock = mockk<PersonClient>()
    val aktorRegisterClientMock = mockk<AktorRegisterHttpClient>()
    val jwtStub = JwtStub()
    private val token = jwtStub.createTokenFor("srvdp-inntekt-api")

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
    fun `Returns 406 if we can't parse organisation id to a Long`() {
        val enhetsRegisterClient = EnhetsRegisteretHttpClient(server.url(""))
        testApp(enhetsRegisterClient) {
            handleRequest(HttpMethod.Get, "api/organisasjon/IkkeEttTall") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
            }.apply {
                Assertions.assertTrue(requestHandled)
                Assertions.assertEquals(HttpStatusCode.NotAcceptable, response.status())
                Assertions.assertEquals(null, response.headers["Cache-Control"])
            }
        }
    }

    @Test
    fun `Returns Organisasjons name if found`() {
        val testAktorId = "974760673"
        WireMock.stubFor(
            WireMock.get(WireMock.urlEqualTo("//enheter/974760673"))
                .willReturn(WireMock.aResponse().withBody(validJsonBodyWithNorskOrg))
        )

        val enhetsRegisterClient = EnhetsRegisteretHttpClient(server.url(""))
        testApp(enhetsRegisterClient) {
            handleRequest(HttpMethod.Get, "api/organisasjon/$testAktorId") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
            }.apply {
                Assertions.assertTrue(requestHandled)
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals("max-age=86400", response.headers["Cache-Control"])
            }
        }
    }

    @Test
    fun `Returns 404 if we can't find the organisation`() {
        val testAktorId = "974760673"
        WireMock.stubFor(
            WireMock.get(WireMock.urlEqualTo("//enheter/974760673"))
                .willReturn(WireMock.notFound())
        )

        val enhetsRegisterClient = EnhetsRegisteretHttpClient(server.url(""))
        testApp(enhetsRegisterClient) {
            handleRequest(HttpMethod.Get, "api/organisasjon/$testAktorId") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
            }.apply {
                Assertions.assertTrue(requestHandled)
                Assertions.assertEquals(HttpStatusCode.NotFound, response.status())
                Assertions.assertEquals(null, response.headers["Cache-Control"])
            }
        }
    }

    val validJsonBodyWithNorskOrg = """
        {"organisasjonsnummer":"974760673",
        "navn":"REGISTERENHETEN I BRØNNØYSUND",
        "organisasjonsform":{"kode":"ORGL",
        "beskrivelse":"Organisasjonsledd",
        "_links":{
            "self":{
                "href":"https://data.brreg.no/enhetsregisteret/api/organisasjonsformer/ORGL"
            }
        }},"hjemmeside":"www.brreg.no",
        "postadresse":{
            "land":"Norge",
            "landkode":"NO",
            "postnummer":"8910",
            "poststed":"BRØNNØYSUND",
            "adresse":["Postboks 900"],
            "kommune":"BRØNNØY",
            "kommunenummer":"1813"
        },"registreringsdatoEnhetsregisteret":"1995-08-09",
          "registrertIMvaregisteret":false,
          "naeringskode1":{
            "beskrivelse":"Generell offentlig administrasjon","kode":"84.110"
            },"antallAnsatte":533,"overordnetEnhet":"912660680",
            "forretningsadresse":{
            "land":"Norge","landkode":"NO","postnummer":"8900","poststed":"BRØNNØYSUND",
            "adresse":["Havnegata 48"],"kommune":"BRØNNØY","kommunenummer":"1813"
            },"institusjonellSektorkode":{"kode":"6100","beskrivelse":"Statsforvaltningen"},
            "registrertIForetaksregisteret":false,"registrertIStiftelsesregisteret":false,
            "registrertIFrivillighetsregisteret":false,"konkurs":false,"underAvvikling":false,
            "underTvangsavviklingEllerTvangsopplosning":false,"maalform":"Bokmål",
            "_links":{"self":{"href":"https://data.brreg.no/enhetsregisteret/api/enheter/974760673"},
            "overordnetEnhet":{"href":"https://data.brreg.no/enhetsregisteret/api/enheter/912660680"}}
            }""".trimIndent()

    private fun testApp(enhetsRegisterClient: EnhetsRegisteretHttpClient, callback: TestApplicationEngine.() -> Unit) {
        val env = Environment(mapOf("JWT_ISSUER" to "test issuer"))

        withTestApplication({
            (oppslag(env, jwtStub.stubbedJwkProvider(), joarkClientSoapMock, personClientMock, aktorRegisterClientMock, enhetsRegisterClient))
        }) { callback() }
    }
}