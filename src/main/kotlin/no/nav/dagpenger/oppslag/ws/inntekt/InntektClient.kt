package no.nav.dagpenger.oppslag.ws.inntekt

import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.OppslagResult
import no.nav.dagpenger.oppslag.Success
import no.nav.tjeneste.virksomhet.inntekt.v3.binding.InntektV3
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Ainntektsfilter
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Formaal
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.PersonIdent
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Uttrekksperiode
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkRequest
import org.slf4j.LoggerFactory
import javax.xml.datatype.DatatypeFactory

class InntektClient(private val inntektV3: InntektV3) {
    private val log = LoggerFactory.getLogger(InntektClient::class.java)

    fun hentInntektListe(aktorid: String): OppslagResult {
        val request = HentInntektListeBolkRequest().apply {
            identListe.add(PersonIdent().apply {
                personIdent = aktorid
            })
            formaal = Formaal().apply {
                // https://confluence.adeo.no/display/FK/Formaal and https://jira.adeo.no/browse/BEGREP-155
                value = "Dagpenger"
            }
            ainntektsfilter = Ainntektsfilter().apply {
                // https://confluence.adeo.no/display/FK/A-inntektsfilter
                value = "Dagpenger" // todo: figure out correct code.
            }
            uttrekksperiode = Uttrekksperiode().apply {
                // todo : create proper filter•
                maanedFom = DatatypeFactory.newInstance().newXMLGregorianCalendar(2017, 12, 1, 0, 0, 0, 0, 0)
                maanedTom = DatatypeFactory.newInstance().newXMLGregorianCalendar(2018, 1, 1, 0, 0, 0, 0, 0)
            }
        }

        return try {
            // todo : map response
            val response = inntektV3.hentInntektListeBolk(request)
            Success(response)
        } catch (ex: Exception) {
            log.error("Error during inntekt lookup", ex)
            Failure(listOf(ex.message ?: "unknown error"))
        }
    }
}