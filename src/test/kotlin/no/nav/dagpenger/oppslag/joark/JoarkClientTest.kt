package no.nav.dagpenger.oppslag.joark

import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.Success
import no.nav.dagpenger.oppslag.ws.joark.JoarkClient
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail

class JoarkClientTest {

    @Test
    fun stubbedLookup() {
        val stubbedClient = JoarkClient(BehandleInngaaendeJournalV1Stub())

        val actual = stubbedClient.ferdigstillJournalføring("123456789")
        when (actual) {
            is Success<*> -> {
                assertEquals(null, actual.data)
            }
            is Failure -> fail("expected this to be successful")
        }
    }

    @Test
    fun stubbedLookupError() {
        val stubbedClient = JoarkClient(BehandleInngaaendeJournalV1MisbehavingStub())

        val expected = Failure(listOf("SOAP-call failed"))

        val actual = stubbedClient.ferdigstillJournalføring("123456789")

        when (actual) {
            is Success<*> -> fail("expected this to fail")
            is Failure -> assertEquals(expected, actual)
        }
    }
}
