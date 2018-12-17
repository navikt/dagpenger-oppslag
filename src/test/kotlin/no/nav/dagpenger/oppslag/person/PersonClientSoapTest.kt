package no.nav.dagpenger.oppslag.person

import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.Success
import no.nav.dagpenger.oppslag.ws.person.GeografiskTilknytningResponse
import no.nav.dagpenger.oppslag.ws.person.PersonClientSoap
import org.junit.Test

class PersonClientSoapTest {

    @Test
    fun stubbedLookup() {
        val stubbedClient = PersonClientSoap(PersonV3Stub())

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
    fun stubbedLookupError() {
        val stubbedClient = PersonClientSoap(PersonV3MisbehavingStub())

        val expected = Failure(listOf("SOAP-call failed"))

        val actual = stubbedClient.getGeografiskTilknytning("123456789")
        when (actual) {
            is Success<*> -> fail("expected this to fail")
            is Failure -> assertEquals(expected, actual)
        }
    }
}