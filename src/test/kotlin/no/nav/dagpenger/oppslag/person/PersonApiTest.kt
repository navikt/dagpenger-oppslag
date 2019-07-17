package no.nav.dagpenger.oppslag.person

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.oppslag.JwtStub
import no.nav.dagpenger.oppslag.Success
import no.nav.dagpenger.oppslag.oppslag
import no.nav.dagpenger.oppslag.ws.aktor.AktorRegisterHttpClient
import no.nav.dagpenger.oppslag.ws.brreg.enhetsregister.EnhetsRegisteretHttpClient
import no.nav.dagpenger.oppslag.ws.joark.JoarkClient
import no.nav.dagpenger.oppslag.ws.person.PersonClient
import no.nav.dagpenger.oppslag.ws.person.PersonNameResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class PersonApiTest {
    private val validJson = """
        {
            "fødselsnummer": "12345678912"
        }
        """.trimIndent()

    private val jsonMissingFields = """
        {}
        """.trimIndent()

    private val jwtStub = JwtStub()
    private val token = jwtStub.createTokenFor("srvdp-inntekt-api")

    private val personClientMock: PersonClient = mockk()
    private val joarkClientSoapMock: JoarkClient = mockk()
    private val aktorRegisterHttpClient: AktorRegisterHttpClient = mockk()
    private val enhetsRegisteretHttpClient: EnhetsRegisteretHttpClient = mockk()

    init {
        every {
            personClientMock.getName(any())
        } returns Success(
            PersonNameResponse(
                etternavn = "etternavntest",
                fornavn = "fornavntest",
                mellomnavn = "mellomnavntest",
                sammensattNavn = "sammensattnavntest"
            )
        )
    }

    @Test
    fun `getName post request with good json`() = testApp {
        handleRequest(HttpMethod.Post, "/api/person/name") {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            setBody(validJson)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("max-age=86400", response.headers["Cache-Control"])
        }
    }

    @Test
    fun `getName post request with bad json`() = testApp {
        handleRequest(HttpMethod.Post, "/api/person/name") {
            addHeader(HttpHeaders.ContentType, "application/json")
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            setBody(jsonMissingFields)
        }.apply {
            assertTrue(requestHandled)
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals(null, response.headers["Cache-Control"])
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        val jwtIssuer = "test issuer"

        withTestApplication({
            (oppslag(
                aktorRegisterClient = aktorRegisterHttpClient,
                enhetRegisterClient = enhetsRegisteretHttpClient,
                joarkClient = joarkClientSoapMock,
                jwkProvider = jwtStub.stubbedJwkProvider(),
                jwtIssuer = jwtIssuer,
                personClient = personClientMock
            ))
        }) { callback() }
    }
}