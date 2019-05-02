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
import no.nav.dagpenger.oppslag.Environment
import no.nav.dagpenger.oppslag.JwtStub
import no.nav.dagpenger.oppslag.Success
import no.nav.dagpenger.oppslag.oppslag
import no.nav.dagpenger.oppslag.ws.joark.JoarkClient
import no.nav.dagpenger.oppslag.ws.person.PersonClientSoap
import no.nav.dagpenger.oppslag.ws.person.PersonNameResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class PersonApiTest {
    val validJson = """
        {
            "fødselsnummer": "12345678912"
        }
        """.trimIndent()

    val jsonMissingFields = """
        {}
        """.trimIndent()

    private val jwtStub = JwtStub()
    private val token = jwtStub.createTokenFor("srvdp-inntekt-api")

    private val personClientSoapMock: PersonClientSoap = mockk()
    private val joarkClientSoapMock: JoarkClient = mockk()

    init {
        every {
            personClientSoapMock.getName(any())
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
            Assertions.assertEquals(HttpStatusCode.OK, response.status())
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
            Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    private fun testApp(callback: TestApplicationEngine.() -> Unit) {
        val env = Environment(mapOf("JWT_ISSUER" to "test issuer"))

        withTestApplication({
            (oppslag(env, jwtStub.stubbedJwkProvider(), joarkClientSoapMock, personClientSoapMock))
        }) { callback() }
    }
}