package no.nav.dagpenger.oppslag.ws.aktor

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
import no.nav.dagpenger.oidc.OidcClient
import no.nav.dagpenger.oidc.OidcToken
import no.nav.dagpenger.oidc.StsOidcClient
import no.nav.dagpenger.oppslag.JwtStub
import no.nav.dagpenger.oppslag.oppslag
import no.nav.dagpenger.oppslag.ws.brreg.enhetsregister.EnhetsRegisteretHttpClient
import no.nav.dagpenger.oppslag.ws.joark.JoarkClient
import no.nav.dagpenger.oppslag.ws.person.PersonClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class AktorRegisterApiTest {
    private val joarkClientSoapMock = mockk<JoarkClient>()
    private val personClientMock = mockk<PersonClient>()
    private val enhetClientMock = mockk<EnhetsRegisteretHttpClient>()
    private val jwtStub = JwtStub()
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

    class DummyOidcClient : OidcClient {
        override fun oidcToken(): OidcToken = OidcToken(UUID.randomUUID().toString(), "openid", 3000)
    }

    @Test
    fun `Returns error if no ident header is set`() {
        val aktorRegisterHttpClient = AktorRegisterHttpClient("", StsOidcClient("", "", ""))
        testApp(aktorRegisterHttpClient) {
            handleRequest(HttpMethod.Get, "api/naturlig-ident") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
            }.apply {
                assertTrue(requestHandled)
                assertEquals(HttpStatusCode.NotAcceptable, response.status())
                assertEquals(null, response.headers["Cache-Control"])
            }
        }
    }

    @Test
    fun `Returns 404 if norsk ident is not found`() {
        val testAktorId = "1234567891234"
        WireMock.stubFor(
            WireMock.get(WireMock.urlEqualTo("//v1/identer?gjeldende=true"))
                .withHeader("Nav-Personidenter", WireMock.equalTo(testAktorId))
                .willReturn(WireMock.aResponse().withBody(validJsonBodyWithoutNorskIdent))
        )

        val aktorRegisterHttpClient = AktorRegisterHttpClient(server.url(""), DummyOidcClient())
        testApp(aktorRegisterHttpClient) {
            handleRequest(HttpMethod.Get, "api/naturlig-ident") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                addHeader("ident", testAktorId)
            }.apply {
                assertTrue(requestHandled)
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertEquals(null, response.headers["Cache-Control"])
            }
        }
    }

    @Test
    fun `Returns norsk ident if found`() {
        val testAktorId = "1234567891234"
        WireMock.stubFor(
            WireMock.get(WireMock.urlEqualTo("//v1/identer?gjeldende=true"))
                .withHeader("Nav-Personidenter", WireMock.equalTo(testAktorId))
                .willReturn(WireMock.aResponse().withBody(validJsonBodyWithNorskIdent))
        )

        val aktorRegisterHttpClient = AktorRegisterHttpClient(server.url(""), DummyOidcClient())
        testApp(aktorRegisterHttpClient) {
            handleRequest(HttpMethod.Get, "api/naturlig-ident") {
                addHeader(HttpHeaders.ContentType, "application/json")
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                addHeader("ident", testAktorId)
            }.apply {
                assertTrue(requestHandled)
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("max-age=86400", response.headers["Cache-Control"])
            }
        }
    }

    private val validJsonBodyWithoutNorskIdent = """
        {
            "1234567891234": {
                "identer": [
                    {
                        "ident": "1234567891234",
                        "identgruppe": "AktoerId",
                        "gjeldende": true
                    }
                ],
                "feilmelding": null
            }
        }
    """.trimIndent()

    private val validJsonBodyWithNorskIdent = """
        {
            "1234567891234": {
                "identer": [
                    {
                        "ident": "1234567891234",
                        "identgruppe": "AktoerId",
                        "gjeldende": true
                    },
                    {
                        "ident": "12345678912",
                        "identgruppe": "NorskIdent",
                        "gjeldende": true
                    }
                ],
                "feilmelding": null
            }
        }
    """.trimIndent()

    private fun testApp(aktorRegisterHttpClient: AktorRegisterHttpClient, callback: TestApplicationEngine.() -> Unit) {
        val jwtIssuer = "test issuer"

        withTestApplication({
            (oppslag(
                aktorRegisterClient = aktorRegisterHttpClient,
                enhetRegisterClient = enhetClientMock,
                joarkClient = joarkClientSoapMock,
                jwkProvider = jwtStub.stubbedJwkProvider(),
                jwtIssuer = jwtIssuer,
                personClient = personClientMock
            ))
        }) { callback() }
    }
}
