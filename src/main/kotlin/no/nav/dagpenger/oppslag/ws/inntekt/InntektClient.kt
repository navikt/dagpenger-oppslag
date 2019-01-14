package no.nav.dagpenger.oppslag.ws.inntekt

import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.OppslagResult
import no.nav.dagpenger.oppslag.Success
import no.nav.tjeneste.virksomhet.inntekt.v3.binding.InntektV3
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.hentinntektliste.ArbeidsInntektInformasjon
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Ainntektsfilter
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Aktoer
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Formaal
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.PersonIdent
import no.nav.tjeneste.virksomhet.inntekt.v3.informasjon.inntekt.Uttrekksperiode
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkRequest
import org.slf4j.LoggerFactory
import java.time.YearMonth
import javax.xml.datatype.DatatypeFactory

class InntektClient(private val inntektV3: InntektV3) {
    private val log = LoggerFactory.getLogger(InntektClient::class.java)

    fun hentInntektListe(aktorid: String): OppslagResult {
        val request = HentInntektListeBolkRequest().apply {
            identListe.add(PersonIdent().apply {
                personIdent = aktorid
            })
            formaal = Formaal().apply { // https://confluence.adeo.no/display/FK/Formaal and https://jira.adeo.no/browse/BEGREP-155
                value = "Dagpenger"
            }
            ainntektsfilter = Ainntektsfilter().apply { // https://confluence.adeo.no/display/FK/A-inntektsfilter
                value = "Dagpengerz" // todo: figure out correct code.
            }
            uttrekksperiode = Uttrekksperiode().apply { // todo : create propor filter
                maanedFom = DatatypeFactory.newInstance().newXMLGregorianCalendar(2017, 12, 1, 0, 0, 0, 0, 0)
                maanedTom = DatatypeFactory.newInstance().newXMLGregorianCalendar(2018, 1, 1, 0, 0, 0, 0, 0)
            }
        }

        try {
            val response = inntektV3.hentInntektListeBolk(request)

            return Success(response)
        } catch (ex: Exception) {
            log.error("Error during inntekt lookup", ex)
            return Failure(listOf(ex.message ?: "unknown error"))
        }
    }

    data class HentInntektListeResponse(
        val inntekter: List<ArbeidsInntektMaaned>
    )
    data class ArbeidsInntektMaaned(
        val årMåned: YearMonth,
        val avvik: List<Avvik>,
        val arbeidsInntektInformasjon: ArbeidsInntektInformasjon
    )
    data class Avvik(
        val ident: Aktoer,
        val opplysningspliktig: Aktoer,
        val virksomhet: Aktoer,
        val avvikPeriode: YearMonth,
        val tekst: String
    )
}
