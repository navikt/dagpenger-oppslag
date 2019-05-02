package no.nav.dagpenger.oppslag.ws.person

import no.nav.dagpenger.oppslag.Failure
import no.nav.dagpenger.oppslag.OppslagResult
import no.nav.dagpenger.oppslag.Success
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import org.slf4j.LoggerFactory

class PersonClientSoap(private val person: PersonV3) {

    private val log = LoggerFactory.getLogger(PersonClientSoap::class.java)

    fun getGeografiskTilknytning(fødselsnummer: String): OppslagResult {
        val request = HentGeografiskTilknytningRequest()

        request.aktoer = PersonIdent().apply {
            ident = NorskIdent().apply { ident = fødselsnummer }
        }

        return try {
            val response = person.hentGeografiskTilknytning(request)
            Success(
                GeografiskTilknytningResponse(
                    response.geografiskTilknytning.geografiskTilknytning,
                    response.diskresjonskode?.value
                )
            )
        } catch (ex: Exception) {
            log.error("Error while finding geografisk tilknytning")
            Failure(listOf(ex.message ?: "unknown"))
        }
    }

    fun getName(fødselsnummer: String): OppslagResult {
        val request = HentPersonRequest()

        request.aktoer = PersonIdent().apply {
            ident = NorskIdent().apply { ident = fødselsnummer }
        }

        return try {
            val response = person.hentPerson(request)
            val navn = response.person.personnavn
            Success(
                PersonNameResponse(
                    navn.etternavn,
                    navn.fornavn,
                    navn.mellomnavn,
                    navn.sammensattNavn
                )
            )
        } catch (ex: Exception) {
            log.error("Error while finding personnavn: $ex")
            Failure(listOf(ex.message ?: "unknown"))
        }
    }
}

data class GeografiskTilknytningResponse(
    val geografiskTilknytning: String,
    val diskresjonskode: String?
)

data class PersonNameResponse(
    val etternavn: String,
    val fornavn: String,
    val mellomnavn: String,
    val sammensattNavn: String
)