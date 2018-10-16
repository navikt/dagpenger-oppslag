package no.nav.dagpenger.oppslag.person

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest

class PersonClientSoap(private val person: PersonV3) : PersonClient {
    override fun getGeografiskTilknytning(fødelsnummer: String): String {
        val request = HentGeografiskTilknytningRequest()
        val aktor = PersonIdent()
        val norskIdent = NorskIdent()

        norskIdent.ident = fødelsnummer
        aktor.ident = norskIdent
        request.aktoer = aktor

        val response = person.hentGeografiskTilknytning(request)

        return response.geografiskTilknytning.geografiskTilknytning
    }
}