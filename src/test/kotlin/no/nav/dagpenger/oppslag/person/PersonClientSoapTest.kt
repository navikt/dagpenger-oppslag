package no.nav.dagpenger.oppslag.person

import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.Success
import no.nav.dagpenger.oppslag.ws.person.GeografiskTilknytningResponse
import no.nav.dagpenger.oppslag.ws.person.PersonClient
import no.nav.dagpenger.oppslag.ws.person.PersonNameResponse
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail

class PersonClientSoapTest {

    @Test
    fun `getGeografiskTiknytning soap success`() {
        val stubbedClient = PersonClient(PersonV3Stub())

        val expected = GeografiskTilknytningResponse("et sted", "2")

        val actual = stubbedClient.getGeografiskTilknytning("123456789")
        when (actual) {
            is Success<*> -> {
                assertEquals(expected, actual.data)
            }
            is Failure -> fail("expected this to be successful")
        }
    }

    @Test
    fun `getGeografiskTilknytning soap error`() {
        val stubbedClient = PersonClient(PersonV3MisbehavingStub())

        val expected = Failure(listOf("SOAP-call failed"))

        val actual = stubbedClient.getGeografiskTilknytning("123456789")
        when (actual) {
            is Success<*> -> fail("expected this to fail")
            is Failure -> assertEquals(expected, actual)
        }
    }

    @Test
    fun `getName soap success`() {
        val stubbedClient = PersonClient(PersonV3Stub())

        val expected = PersonNameResponse(
            "etternavntest",
            "fornavntest",
            "mellomnavntest",
            "sammensattnavntest"
        )

        val actual = stubbedClient.getName("123456789")
        when (actual) {
            is Success<*> -> {
                assertEquals(expected, actual.data)
            }
            is Failure -> fail("expected this to be successful")
        }
    }

    @Test
    fun `getName soap Error`() {
        val stubbedClient = PersonClient(PersonV3MisbehavingStub())

        val expected = Failure(listOf("SOAP-call failed"))

        val actual = stubbedClient.getName("123456789")
        when (actual) {
            is Success<*> -> fail("expected this to fail")
            is Failure -> assertEquals(expected, actual)
        }
    }
}
